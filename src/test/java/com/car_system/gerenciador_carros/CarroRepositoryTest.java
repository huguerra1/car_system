package com.car_system.gerenciador_carros;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CarroRepositoryTest {
    @Autowired
    private CarroRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persisteConsultaAtualizaEExclui() {
        Carro carro = carro("Toyota", "Corolla", 2020);
        repository.saveAndFlush(carro);
        Long id = carro.getId();
        assertThat(id).isNotNull();
        entityManager.clear();

        Carro encontrado = repository.findById(id).orElseThrow();
        assertThat(encontrado.getMarca()).isEqualTo("Toyota");
        assertThat(encontrado.getModelo()).isEqualTo("Corolla");
        assertThat(encontrado.getAno()).isEqualTo(2020);

        encontrado.setModelo("Yaris");
        repository.saveAndFlush(encontrado);
        entityManager.clear();
        assertThat(repository.findById(id).orElseThrow().getModelo()).isEqualTo("Yaris");

        repository.deleteById(id);
        repository.flush();
        entityManager.clear();
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void listaEmOrdemCrescenteDeId() {
        Carro primeiro = repository.saveAndFlush(carro("Toyota", "Corolla", 2020));
        Carro segundo = repository.saveAndFlush(carro("Fiat", "Uno", 2010));
        entityManager.clear();

        assertThat(repository.findAll(Sort.by("id")))
                .extracting(Carro::getId)
                .containsExactly(primeiro.getId(), segundo.getId());
    }

    private Carro carro(String marca, String modelo, int ano) {
        Carro carro = new Carro();
        carro.setMarca(marca);
        carro.setModelo(modelo);
        carro.setAno(ano);
        return carro;
    }
}
