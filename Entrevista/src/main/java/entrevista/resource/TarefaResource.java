package entrevista.resource;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import entrevista.model.Pessoa;
import entrevista.model.Tarefa;
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

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public List<PessoaDTO> listarPessoas() {
        List<Pessoa> pessoas = Pessoa.listAll(Sort.by("nome"));

        return pessoas.stream()
                .map(this::mapToPessoaDTO)
                .collect(Collectors.toList());
    }

    private PessoaDTO mapToPessoaDTO(Pessoa pessoa) {
        PessoaDTO pessoaDTO = new PessoaDTO();
        pessoaDTO.id = pessoa.id;
        pessoaDTO.nome = pessoa.nome;
        pessoaDTO.departamento = pessoa.departamento;
        pessoaDTO.totalHorasTarefas = calcularTotalHorasTarefas(pessoa);
        return pessoaDTO;
    }

    private long calcularTotalHorasTarefas(Pessoa pessoa) {
        return pessoa.getTarefas().stream()
                .mapToLong(tarefa -> calcularDuracaoTarefaEmHoras(tarefa))
                .sum();
    }

    private long calcularDuracaoTarefaEmHoras(Tarefa tarefa) {
        // Converte a duração de dias para horas
        long duracaoEmHoras = tarefa.getDuracao().toHours();

        // Calcula a diferença em horas entre o prazo e a data atual
        long horasRestantes = Duration.between(tarefa.getPrazo().atStartOfDay(), LocalDate.now().atStartOfDay()).toHours();

        // Retorna a duração em horas, limitada ao prazo
        return duracaoEmHoras > horasRestantes ? horasRestantes : duracaoEmHoras;
    }

    public static class PessoaDTO {
        public Long id;
        public String nome;
        public String departamento;
        public long totalHorasTarefas;
    }
}
    

    

