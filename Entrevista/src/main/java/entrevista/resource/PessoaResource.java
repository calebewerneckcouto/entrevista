package entrevista.resource;



import java.util.List;
import java.util.stream.Collectors;

import entrevista.model.Pessoa;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/pessoas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PessoaResource {

	@POST
	@Transactional
	public Pessoa adicionarPessoa(Pessoa pessoa) {
	    Pessoa.persist(pessoa);
	    return pessoa;
	}


    @PUT
    @Transactional
    @Path("/{id}")
    public Pessoa alterarPessoa(@PathParam("id") Long id, Pessoa pessoa) {
        Pessoa entity = Pessoa.findById(id);
        if (entity != null) {
            entity.nome = pessoa.nome;
            entity.departamento = pessoa.departamento;
            entity.persist();
        }
        return entity;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response removerPessoa(@PathParam("id") Long id) {
        Pessoa pessoa = Pessoa.findById(id);
        if (pessoa != null) {
            pessoa.delete();
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Pessoa não encontrada para o ID: " + id).build();
        }
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
                .mapToLong(tarefa -> tarefa.getDuracao().toHours())
                .sum();
    }

    public static class PessoaDTO {
        public Long id;
        public String nome;
        public String departamento;
        public long totalHorasTarefas;
    }}
    
   
    

