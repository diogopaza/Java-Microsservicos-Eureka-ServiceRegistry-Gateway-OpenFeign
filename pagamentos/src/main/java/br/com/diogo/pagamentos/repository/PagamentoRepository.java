package br.com.diogo.pagamentos.repository;

import br.com.diogo.pagamentos.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PagamentoRepository  extends JpaRepository<Pagamento, Long>{

}
