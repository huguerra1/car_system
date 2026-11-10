package com.car_system.gerenciador_carros;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CarroIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarroRepository repository;

    @BeforeEach
    void prepararBanco() {
        repository.deleteAll();
    }

    @AfterEach
    void limparBanco() {
        repository.deleteAll();
    }

    @Test
    void executaCrudCompleto() throws Exception {
        mockMvc.perform(get("/carros"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        String location = mockMvc.perform(post("/carros").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"marca":"Toyota","modelo":"Corolla","ano":2020}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andReturn().getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        String caminho = java.net.URI.create(location).getPath();
        Long id = Long.valueOf(caminho.substring(caminho.lastIndexOf('/') + 1));
        assertThat(repository.findById(id)).isPresent();

        String segundaLocation = mockMvc.perform(post("/carros").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"marca":"Fiat","modelo":"Uno","ano":2010}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        assertThat(segundaLocation).isNotNull();
        String segundoCaminho = java.net.URI.create(segundaLocation).getPath();
        Long segundoId = Long.valueOf(segundoCaminho.substring(segundoCaminho.lastIndexOf('/') + 1));

        mockMvc.perform(get(caminho))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.modelo").value("Corolla"))
                .andExpect(jsonPath("$.ano").value(2020));
        mockMvc.perform(get("/carros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[1].id").value(segundoId));

        String atualizacao = """
                {"marca":"Honda","modelo":"Civic","ano":2024}
                """;
        mockMvc.perform(put(caminho).contentType(MediaType.APPLICATION_JSON).content(atualizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.marca").value("Honda"))
                .andExpect(jsonPath("$.modelo").value("Civic"))
                .andExpect(jsonPath("$.ano").value(2024));
        mockMvc.perform(get(caminho))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Civic"));

        mockMvc.perform(delete(caminho))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        assertThat(repository.findById(id)).isEmpty();
        mockMvc.perform(get(caminho)).andExpect(status().isNotFound());
        mockMvc.perform(put(caminho).contentType(MediaType.APPLICATION_JSON).content(atualizacao))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete(caminho)).andExpect(status().isNotFound());
        mockMvc.perform(delete(segundoCaminho)).andExpect(status().isNoContent());
        mockMvc.perform(get("/carros"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void dadosInvalidosNaoSaoPersistidos() throws Exception {
        mockMvc.perform(post("/carros").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"marca":" ","modelo":"","ano":1885}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.marca").isArray())
                .andExpect(jsonPath("$.erros.modelo").isArray())
                .andExpect(jsonPath("$.erros.ano").isArray());
        assertThat(repository.count()).isZero();
    }
}
