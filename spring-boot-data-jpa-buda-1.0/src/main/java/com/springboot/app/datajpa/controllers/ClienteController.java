package com.springboot.app.datajpa.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.app.datajpa.models.entity.Cliente;
import com.springboot.app.datajpa.models.service.IClienteService;
import com.springboot.app.datajpa.util.paginator.PageRender;

@Controller
@SessionAttributes("cliente")
public class ClienteController {

	@Autowired
	private IClienteService clienteService;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * Metodo de la capa vista que se encarga de listar todos los clientes.
	 * @param model - modelo de la vista
	 * @return listar - pagina que visualiza la lista de clientes
	 */
	@RequestMapping(value = "/listar", method = RequestMethod.GET)
	public String listar(@RequestParam(name="page", defaultValue="0") int page, Model model) {
		Pageable pageRequest = 	new PageRequest(page, 5);
		
		Page<Cliente> clientes = clienteService.findAll(pageRequest);
		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);
		
		model.addAttribute("titulo", "Listado de clientes");
		model.addAttribute("clientes",clientes);
		model.addAttribute("page", pageRender);
		return "listar";
	} 
	
	/**
	 * Metodo de la capa vista que se encarga de crear nuevos clientes.
	 * @param model - modelo de la vista
	 * @return form - pagina que visualiza el formulario para dar de alta clientes
	 */
	@RequestMapping(value = "/form")
	public String crear(Map<String, Object> model) {
		Cliente cliente = new Cliente();
		model.put("cliente", cliente);
		model.put("titulo", "Formulario de cliente");
		return "form";
	}
	
	/**
	 * Metodo de la capa vista que se encarga de guardar los cambios de un nuevo cliente.
	 * @param cliente - objeto de tipo Cliente
	 * @param result - objeto de tipo BindingResult, se usa para saber si el resultado vuelve con errores
	 * @param model - modelo de la vista
	 * @param sessionStatus
	 * @return
	 */
	@RequestMapping(value = "/form", method = RequestMethod.POST)
	public String guardar(@Valid Cliente cliente, BindingResult result, Model model, @RequestParam("file") MultipartFile foto,
			RedirectAttributes redirectAttributes, SessionStatus sessionStatus) {

		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de cliente");
			return "form";
		}
		
		if(!foto.isEmpty()) {
			String uniqueFilename = UUID.randomUUID().toString() + foto.getOriginalFilename();
			
			Path rootPath = Paths.get("uploads").resolve(uniqueFilename);
			Path rootAbsolutePath = rootPath.toAbsolutePath();
			
			logger.info("rootPath: " + rootPath);
			logger.info("rootAbsolutePath: " + rootAbsolutePath);
			
			try {
				Files.copy(foto.getInputStream(), rootAbsolutePath);
				redirectAttributes.addFlashAttribute("info", "Has subido correctamente '" + uniqueFilename + "'");
				
				cliente.setFoto(uniqueFilename);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con éxito" : "Cliente creado con éxito";
		redirectAttributes.addFlashAttribute("success", mensajeFlash);
		clienteService.save(cliente);
		sessionStatus.setComplete();
		
		return "redirect:listar";
	}
	
	@RequestMapping(value = "/form/{id}")
	public String editar(@PathVariable (value = "id") Long id, RedirectAttributes redirectAttributes, Map<String, Object> model) {
		
		Cliente cliente = null;
		
		if(id > 0) {
			cliente = clienteService.findOne(id);
			if(cliente == null) {
				redirectAttributes.addFlashAttribute("error", "El cliente no existe");
				return "redirect:/listar";
			}
		} else {
			redirectAttributes.addFlashAttribute("error", "El ID del cliente no puede ser cero");
			return "redirect:/listar";
		}
		model.put("cliente", cliente);
		model.put("titulo", "Editar cliente");
		return "form";
	}
	
	@RequestMapping(value = "/eliminar/{id}")
	public String eliminar(@PathVariable (value = "id") Long id, RedirectAttributes redirectAttributes, Map<String, Object> model) {
		
		if(id > 0) {
			clienteService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Cliente eliminado con éxito");
		}
		return "redirect:/listar";
	}
	
	/**
	 * Metodo de la capa de vista que se encarga de mostrar los datos del cliente, incluyendo una imagen del cliente.
	 * 
	 * @param id - id del cliente
	 * @param model - modelo de la vista
	 * @param flash - atributos de redireccion
	 * @return ver- formulario de detalle de cliente
	 */
	@RequestMapping(value = "/ver/{id}")
	public String ver(@PathVariable(value="id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		
		Cliente cliente = clienteService.findOne(id);
		
		if(cliente == null) {
			flash.addFlashAttribute("error", "El cliente no se encontró en la base de datos");
			return "redirect:/listar";
		}
		
		model.put("cliente", cliente);
		model.put("titulo", "Detalle de cliente: " + cliente.getNombre());
		
		return "ver";
	}
}
