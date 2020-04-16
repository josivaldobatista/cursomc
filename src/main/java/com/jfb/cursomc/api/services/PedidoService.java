package com.jfb.cursomc.api.services;

import java.util.Date;
import java.util.Optional;

import com.jfb.cursomc.api.domain.ItemPedido;
import com.jfb.cursomc.api.domain.PagamentoComBoleto;
import com.jfb.cursomc.api.domain.Pedido;
import com.jfb.cursomc.api.domain.enums.EstadoPagamento;
import com.jfb.cursomc.api.repositories.ItemPedidoRepository;
import com.jfb.cursomc.api.repositories.PagamentoRepository;
import com.jfb.cursomc.api.repositories.PedidoRepository;
import com.jfb.cursomc.api.services.exceptions.ObjectNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository repo;

    @Autowired
    private BoletoService boletoService;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private ProdutoService produtoService;
    
    @Autowired
    private ItemPedidoRepository itemPedidoRepository;

    @Autowired
	private ClienteService clienteService;

    public Pedido find(Integer id) {
        Optional<Pedido> obj = repo.findById(id);
        return obj.orElseThrow(() -> new ObjectNotFoundException(
                "Objecto não encontrado! ID: " + id + ", Tipo: " + Pedido.class.getName()));
    }

    /**
     * Obs.: No caso de um pagamento com boleto, vai precisar configurar a 
     * data de vencimento, e caso reais, seria gerado uma chamada a um webservice
     * (que gera um boleto). Então para fins didáticos, eu criei uma classe chamada
     * BoletoService para simular uma data de vencimento para 7 dias.
     */
    @Transactional
	public Pedido insert(Pedido obj) {
        obj.setId(null);
        obj.setInstante(new Date());
        obj.setCliente(clienteService.find(obj.getCliente().getId()));
        obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
        obj.getPagamento().setPedido(obj);
        if(obj.getPagamento() instanceof PagamentoComBoleto) {
            PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
            boletoService.preencherPagamentoComBoleto(pagto, obj.getInstante());
        }
        obj = repo.save(obj);
        pagamentoRepository.save(obj.getPagamento());
        for(ItemPedido ip : obj.getItens()) {
            ip.setDesconto(0.0);
            ip.setProduto(produtoService.find(ip.getProduto().getId()));
            ip.setProduto(produtoService.find(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
        }
        itemPedidoRepository.saveAll(obj.getItens());
        System.out.println(obj);
        return obj;
	}

}