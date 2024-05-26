package br.com.diogo.pagamentos.controller;

import br.com.diogo.pagamentos.dto.PagamentoDto;
import br.com.diogo.pagamentos.http.PedidoEndpointService;
import br.com.diogo.pagamentos.service.PagamentoService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private PedidoEndpointService pedidoEndpointService;


    @GetMapping
    public Page<PagamentoDto> listar(@PageableDefault(size = 10) Pageable paginacao) {
        return pagamentoService.obterTodos(paginacao);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoDto> detalhar(@PathVariable @NotNull Long id) {
        PagamentoDto dto = pagamentoService.obterPagamentoPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<PagamentoDto> cadastrar(@RequestBody @Valid PagamentoDto dto, UriComponentsBuilder uriBuilder) {
        PagamentoDto pagamento = pagamentoService.criarPagamento(dto);
        URI endereco = uriBuilder.path("/pagamentos/{id}").buildAndExpand(pagamento.getId()).toUri();
        //Testes com Open Feign
        pedidoEndpointService.todosPedidos().stream()
                .forEach(pedidoDto -> System.out.println(pedidoDto.getDataHora()));
        pedidoEndpointService.aprovarPagamentoPedido(dto.getPedidoId());
        return ResponseEntity.created(endereco).body(pagamento);
    }
    @PutMapping("/{id}")
    public ResponseEntity<PagamentoDto> alterar(@PathVariable @NotNull Long id,
                                                @RequestBody PagamentoDto dto){
        var atualizado = pagamentoService.atualizarPagamento(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PagamentoDto> remover(@PathVariable @NotNull Long id){
        pagamentoService.excluirPagamento(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("porta-instancia")
    public ResponseEntity<?> retornarPortaInstanciaPagamentoMS(
            @Value("${local.server.port}") String porta) {
        return ResponseEntity.ok().body("Instancia na porta: " + porta);
    }

    @PatchMapping("{idPagamento}/confirmar")
    @CircuitBreaker(name = "atualizaPedido", fallbackMethod = "pagamentoAutorizadoComIntegracaoPendente")
    public ResponseEntity<?> confimarPagamento(@PathVariable("idPagamento") Long idPagamento){
        var pagamento = pagamentoService.confirmarPagamento(idPagamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(pagamento);
    }

    public ResponseEntity<?> pagamentoAutorizadoComIntegracaoPendente(Long id, Exception e){
        return ResponseEntity.ok().body(pagamentoService.confirmarPagamentoSemIntegracao(id));
    }
}
