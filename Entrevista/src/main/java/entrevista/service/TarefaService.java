package entrevista.service;

import java.util.List;
import java.util.stream.Collectors;

import entrevista.model.Tarefa;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TarefaService {

    @Transactional
    public List<Tarefa> listarTarefasSemAlocacaoMaisAntigas(int quantidade) {
        // Obtém todas as tarefas
        List<Tarefa> todasTarefas = Tarefa.listAll();

        // Filtra as tarefas sem pessoa alocada
        List<Tarefa> tarefasSemAlocacao = todasTarefas.stream()
                .filter(tarefa -> tarefa.getPessoa() == null)
                .collect(Collectors.toList());

        // Ordena as tarefas pelo prazo
        tarefasSemAlocacao.sort((t1, t2) -> t1.prazo.compareTo(t2.prazo));

        // Retorna as três primeiras
        return tarefasSemAlocacao.stream().limit(quantidade).collect(Collectors.toList());
    }
}
