package com.springboot.app.datajpa.models.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.springboot.app.datajpa.models.entity.Cliente;

public interface IClienteDao extends PagingAndSortingRepository<Cliente, Long>{

	
}
