package com.academia.backend.model;

import com.academia.backend.model.enums.NivelTreino;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Plano de treino imutável, construído por meio do padrão Builder.
 */
public final class PlanoTreino {

    private final UUID id;
    private final UUID alunoId;
    private final String nome;
    private final String objetivo;
    private final int frequenciaSemanal;
    private final NivelTreino nivel;
    private final int duracaoSemanas;
    private final List<Exercicio> exercicios;
    private final String observacoes;
    private final LocalDate dataCriacao;

    private PlanoTreino(Builder builder) {
        this.id = UUID.randomUUID();
        this.alunoId = builder.alunoId;
        this.nome = builder.nome;
        this.objetivo = builder.objetivo;
        this.frequenciaSemanal = builder.frequenciaSemanal;
        this.nivel = builder.nivel;
        this.duracaoSemanas = builder.duracaoSemanas;
        // Impede que alterações posteriores na lista do Builder alterem o plano criado.
        this.exercicios = List.copyOf(builder.exercicios);
        this.observacoes = builder.observacoes;
        this.dataCriacao = LocalDate.now();
    }

    public static Builder builder(UUID alunoId, String nome, String objetivo) {
        return new Builder(alunoId, nome, objetivo);
    }

    public UUID getId() {
        return id;
    }

    public UUID getAlunoId() {
        return alunoId;
    }

    public String getNome() {
        return nome;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public int getFrequenciaSemanal() {
        return frequenciaSemanal;
    }

    public NivelTreino getNivel() {
        return nivel;
    }

    public int getDuracaoSemanas() {
        return duracaoSemanas;
    }

    public List<Exercicio> getExercicios() {
        return exercicios;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public static final class Builder {

        private final UUID alunoId;
        private final String nome;
        private final String objetivo;
        private int frequenciaSemanal = 3;
        private NivelTreino nivel = NivelTreino.INICIANTE;
        private int duracaoSemanas = 4;
        private final List<Exercicio> exercicios = new ArrayList<>();
        private String observacoes = "Nenhuma";

        private Builder(UUID alunoId, String nome, String objetivo) {
            this.alunoId = Objects.requireNonNull(alunoId, "O alunoId é obrigatório.");
            this.nome = validarTexto(nome, "O nome do plano é obrigatório.");
            this.objetivo = validarTexto(objetivo, "O objetivo do plano é obrigatório.");
        }

        public Builder frequenciaSemanal(int frequenciaSemanal) {
            this.frequenciaSemanal = frequenciaSemanal;
            return this;
        }

        public Builder nivel(NivelTreino nivel) {
            this.nivel = Objects.requireNonNull(nivel, "O nível do treino é obrigatório.");
            return this;
        }

        public Builder duracaoSemanas(int duracaoSemanas) {
            this.duracaoSemanas = duracaoSemanas;
            return this;
        }

        public Builder adicionarExercicio(Exercicio exercicio) {
            this.exercicios.add(Objects.requireNonNull(exercicio, "O exercício não pode ser nulo."));
            return this;
        }

        public Builder observacoes(String observacoes) {
            this.observacoes = validarTexto(observacoes, "As observações não podem ser vazias.");
            return this;
        }

        public PlanoTreino build() {
            if (frequenciaSemanal < 1 || frequenciaSemanal > 7) {
                throw new IllegalStateException("A frequência semanal deve estar entre 1 e 7 dias.");
            }
            if (duracaoSemanas < 1 || duracaoSemanas > 52) {
                throw new IllegalStateException("A duração deve estar entre 1 e 52 semanas.");
            }
            if (exercicios.isEmpty()) {
                throw new IllegalStateException("O plano deve possuir ao menos um exercício.");
            }
            return new PlanoTreino(this);
        }

        private static String validarTexto(String valor, String mensagem) {
            if (valor == null || valor.isBlank()) {
                throw new IllegalArgumentException(mensagem);
            }
            return valor.trim();
        }
    }
}
