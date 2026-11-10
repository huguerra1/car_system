package com.car_system.gerenciador_carros;

import java.time.Year;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarroController.class)
class CarroControllerTest {
    private static final String VALIDO = """
            {"marca":"Toyota","modelo":"Corolla","ano":2020}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarroService service;

    @Test
    void listaCarros() throws Exception {
        when(service.listar()).thenReturn(List.of(carro()));

        mockMvc.perform(get("/carros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].marca").value("Toyota"));
    }

    @Test
    void buscaCarro() throws Exception {
        when(service.buscar(1L)).thenReturn(carro());

        mockMvc.perform(get("/carros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.modelo").value("Corolla"))
                .andExpect(jsonPath("$.ano").value(2020));
    }

    @Test
    void criaComLocation() throws Exception {
        when(service.criar(new CarroEntrada("Toyota", "Corolla", 2020))).thenReturn(carro());

        mockMvc.perform(post("/carros").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/carros/1"))
                .andExpect(jsonPath("$.id").value(1));
        verify(service).criar(new CarroEntrada("Toyota", "Corolla", 2020));
    }

    @Test
    void atualizaCarro() throws Exception {
        when(service.atualizar(1L, new CarroEntrada("Toyota", "Corolla", 2020))).thenReturn(carro());

        mockMvc.perform(put("/carros/1").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.marca").value("Toyota"));
        verify(service).atualizar(1L, new CarroEntrada("Toyota", "Corolla", 2020));
    }

    @Test
    void excluiSemCorpo() throws Exception {
        mockMvc.perform(delete("/carros/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).excluir(1L);
    }

    @Test
    void retorna404AoBuscarAtualizarOuExcluirIdInexistente() throws Exception {
        when(service.buscar(99L)).thenThrow(new CarroNaoEncontradoException(99L));
        when(service.atualizar(99L, new CarroEntrada("Toyota", "Corolla", 2020)))
                .thenThrow(new CarroNaoEncontradoException(99L));
        doThrow(new CarroNaoEncontradoException(99L)).when(service).excluir(99L);

        mockMvc.perform(get("/carros/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Carro com id 99 não encontrado"));
        mockMvc.perform(put("/carros/99").contentType(MediaType.APPLICATION_JSON).content(VALIDO))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/carros/99"))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("entradasInvalidas")
    void rejeitaCamposInvalidosNoPostEPut(String json, String campo) throws Exception {
        mockMvc.perform(post("/carros").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erros." + campo + "[0]").isNotEmpty());
        mockMvc.perform(put("/carros/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros." + campo + "[0]").isNotEmpty());
        verifyNoInteractions(service);
    }

    static Stream<Arguments> entradasInvalidas() {
        return Stream.of(
                Arguments.of("{}", "marca"),
                Arguments.of("{}", "modelo"),
                Arguments.of("{}", "ano"),
                Arguments.of("{\"marca\":null,\"modelo\":\"Corolla\",\"ano\":2020}", "marca"),
                Arguments.of("{\"marca\":\"Toyota\",\"modelo\":null,\"ano\":2020}", "modelo"),
                Arguments.of("{\"marca\":\"\",\"modelo\":\"Corolla\",\"ano\":2020}", "marca"),
                Arguments.of("{\"marca\":\"Toyota\",\"modelo\":\"\",\"ano\":2020}", "modelo"),
                Arguments.of("{\"marca\":\"   \",\"modelo\":\"Corolla\",\"ano\":2020}", "marca"),
                Arguments.of("{\"marca\":\"Toyota\",\"modelo\":\"   \",\"ano\":2020}", "modelo"),
                Arguments.of("{\"marca\":\"%s\",\"modelo\":\"Corolla\",\"ano\":2020}".formatted("a".repeat(61)), "marca"),
                Arguments.of("{\"marca\":\"Toyota\",\"modelo\":\"%s\",\"ano\":2020}".formatted("a".repeat(61)), "modelo"),
                Arguments.of("{\"marca\":\"Toyota\",\"modelo\":\"Corolla\",\"ano\":null}", "ano"),
                Arguments.of("{\"marca\":\"Toyota\",\"modelo\":\"Corolla\",\"ano\":1885}", "ano"),
                Arguments.of("{\"marca\":\"Toyota\",\"modelo\":\"Corolla\",\"ano\":%d}".formatted(Year.now().getValue() + 2), "ano"));
    }

    @ParameterizedTest
    @MethodSource("anosLimite")
    void aceitaLimitesInclusivosDeAnoETexto(int ano) throws Exception {
        when(service.criar(any())).thenReturn(carro());
        String texto = "a".repeat(60);
        String json = "{\"marca\":\"%s\",\"modelo\":\"%s\",\"ano\":%d}".formatted(texto, texto, ano);

        mockMvc.perform(post("/carros").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
        verify(service).criar(new CarroEntrada(texto, texto, ano));
    }

    static Stream<Integer> anosLimite() {
        return Stream.of(1886, Year.now().getValue() + 1);
    }

    @Test
    void retorna400ParaJsonInvalido() throws Exception {
        mockMvc.perform(post("/carros").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(service);
    }

    private Carro carro() {
        Carro carro = new Carro();
        carro.setId(1L);
        carro.setMarca("Toyota");
        carro.setModelo("Corolla");
        carro.setAno(2020);
        return carro;
    }
}
