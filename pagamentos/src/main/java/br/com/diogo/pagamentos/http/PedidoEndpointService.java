package br.com.diogo.pagamentos.http;

import br.com.diogo.pagamentos.dto.PedidoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Service
@FeignClient(name = "PEDIDOS-MS", url = "http://localhost:8082/pedidos-ms/pedidos")
public interface PedidoEndpointService {

    @RequestMapping(method = RequestMethod.GET, value = "")
    List<PedidoDto> todosPedidos();

    @RequestMapping(method = RequestMethod.PUT, value = "/{idPedido}/pago")
    PedidoDto aprovarPagamentoPedido(@PathVariable("idPedido") Long idPedido);
}
