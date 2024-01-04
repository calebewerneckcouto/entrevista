package entrevista.resource;



import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import entrevista.model.Pessoa;
import entrevista.model.Tarefa;
import entrevista.resource.PessoaResource.DepartamentoDTO;
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
    @Path("/departamentos")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DepartamentoDTO> listarDepartamentosPessoa() {
        List<Pessoa> pessoas = Pessoa.listAll();
        List<Tarefa> tarefas = Tarefa.listAll();

        Map<String, Long> quantidadePessoasPorDepartamento = pessoas.stream()
                .collect(Collectors.groupingBy(Pessoa::getDepartamento, Collectors.counting()));

        Map<String, Long> quantidadeTarefasPorDepartamento = tarefas.stream()
                .collect(Collectors.groupingBy(Tarefa::getDepartamento, Collectors.counting()));

        return quantidadePessoasPorDepartamento.entrySet().stream()
                .map(entry -> new DepartamentoDTO(entry.getKey(), entry.getValue(),
                        quantidadeTarefasPorDepartamento.getOrDefault(entry.getKey(), 0L)))
                .collect(Collectors.toList());
    }

    public static class DepartamentoDTO {
        private String nome;
        private Long quantidadePessoas;
        private Long quantidadeTarefas;

        public DepartamentoDTO(String nome, Long quantidadePessoas, Long quantidadeTarefas) {
            this.nome = nome;
            this.quantidadePessoas = quantidadePessoas;
            this.quantidadeTarefas = quantidadeTarefas;
        }

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public Long getQuantidadePessoas() {
			return quantidadePessoas;
		}

		public void setQuantidadePessoas(Long quantidadePessoas) {
			this.quantidadePessoas = quantidadePessoas;
		}

		public Long getQuantidadeTarefas() {
			return quantidadeTarefas;
		}

		public void setQuantidadeTarefas(Long quantidadeTarefas) {
			this.quantidadeTarefas = quantidadeTarefas;
		}

        
    }
}