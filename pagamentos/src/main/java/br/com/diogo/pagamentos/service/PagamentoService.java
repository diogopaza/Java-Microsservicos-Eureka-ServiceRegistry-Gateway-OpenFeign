package br.com.diogo.pagamentos.service;

import br.com.diogo.pagamentos.dto.PagamentoDto;
import br.com.diogo.pagamentos.http.PedidoEndpointService;
import br.com.diogo.pagamentos.model.Pagamento;
import br.com.diogo.pagamentos.model.Status;
import br.com.diogo.pagamentos.repository.PagamentoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class PagamentoService {
    @Autowired
    private PagamentoRepository pagamentoRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PedidoEndpointService pedidoEndpointService;
    public Page<PagamentoDto> obterTodos(Pageable paginacao) {
        return pagamentoRepository
                .findAll(paginacao)
                .map(p -> modelMapper.map(p, PagamentoDto.class));
    }

    public PagamentoDto obterPagamentoPorId(Long id){
        Pagamento pagamento = pagamentoRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
        return modelMapper.map(pagamento, PagamentoDto.class);
    }

    public PagamentoDto criarPagamento(PagamentoDto dto){
        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
        pagamento.setStatus(Status.CRIADO);
        pagamentoRepository.save(pagamento);
        return modelMapper.map(pagamento, PagamentoDto.class);
    }

    public PagamentoDto atualizarPagamento(Long id, PagamentoDto dto){
        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
        pagamento.setId(id);
        pagamentoRepository.save(pagamento);
        return modelMapper.map(pagamento, PagamentoDto.class);
    }

    public void excluirPagamento(Long id){
        pagamentoRepository.deleteById(id);
    }

    public Pagamento confirmarPagamento(Long id){
        Optional pagamento = pagamentoRepository.findById(id);
        if(!pagamento.isPresent()){
            throw new EntityNotFoundException("Pagamento não localizado!");
        }
        var pagamentoModel = modelMapper.map(pagamento, Pagamento.class);
        pagamentoModel.setStatus(Status.CONFIRMADO);
        pedidoEndpointService.aprovarPagamentoPedido(pagamentoModel.getPedidoId());
        var pagamentoFinal = pagamentoRepository.save(pagamentoModel);
        return pagamentoFinal;
    }

    public Pagamento confirmarPagamentoSemIntegracao(Long id){
        Optional pagamento = pagamentoRepository.findById(id);
        if(!pagamento.isPresent()){
            throw new EntityNotFoundException("Pagamento não localizado!");
        }
        var pagamentoModel = modelMapper.map(pagamento, Pagamento.class);
        pagamentoModel.setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
        var pagamentoFinal = pagamentoRepository.save(pagamentoModel);
        return pagamentoFinal;
    }


}
