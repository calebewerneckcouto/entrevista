package entrevista.resource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import entrevista.model.Pessoa;
import entrevista.model.Tarefa;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/tarefas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TarefaResource {

    @POST
    @Transactional
    public Tarefa adicionarTarefa(Tarefa tarefa) {
        Tarefa.persist(tarefa);
        return tarefa;
    }

    @PUT
    @Transactional
    @Path("/alocar/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response alocarPessoaNaTarefa(@PathParam("id") Long idTarefa, Pessoa pessoa) {
        // Verificar se a tarefa existe
        Tarefa tarefa = Tarefa.findById(idTarefa);
        if (tarefa == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Tarefa não encontrada").build();
        }

        // Verificar se a pessoa existe
        Pessoa pessoaParaAlocar = Pessoa.findById(pessoa.id);
        if (pessoaParaAlocar == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Pessoa não encontrada").build();
        }

        // Verificar se a pessoa tem o mesmo departamento que a tarefa
        if (!tarefa.getDepartamento().equals(pessoaParaAlocar.getDepartamento())) {
            return Response.status(Response.Status.BAD_REQUEST).entity("A pessoa não pertence ao mesmo departamento da tarefa").build();
        }

        // Alocar a pessoa na tarefa
        tarefa.setPessoa(pessoaParaAlocar);
        tarefa.persist();

        return Response.ok(tarefa).build();
    }

    @PUT
    @Path("/finalizar/{id}")
    @Transactional
    public Tarefa finalizarTarefa(@PathParam("id") Long id) {
        Tarefa tarefa = Tarefa.findById(id);
        if (tarefa != null) {
            tarefa.setFinalizado(true);
            Tarefa.persist(tarefa);
        }
        return tarefa;
    }

    @GET
    @Path("/pendentes")
    public List<Tarefa> listarTarefasPendentes() {
        return Tarefa.list("finalizado = false", Sort.ascending("prazo"));
    }
    
    
}
    

