package org.springframework.samples.petclinic.incident;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Controller
public class IncidentController{


	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	private static final String VIEWS_INCIDENT_CREATE_OR_UPDATE_FORM = "incidents/createOrUpdateIncidentForm";

	private final IncidentRepository incidents;

	public IncidentController(IncidentRepository incidents) {
		this.incidents= incidents;
	}

	@ModelAttribute("incident_types")
	public Collection<IncidentType> populatePetTypes() {
		return this.incidents.findIncidentTypes();
	}

	@ModelAttribute("incident")
	public Incident findIncident(@PathVariable(name = "incidentId", required = false) Integer incidentId) {
		return incidentId == null ? new Incident()
			: this.incidents.findById(incidentId)
			.orElseThrow(() -> new IllegalArgumentException("Incident not found with id: " + incidentId
				+ ". Please ensure the ID is correct " + "and the incident exists in the database."));
	}

//we are returning a new incdent form creator
	@GetMapping("/incidents/new")
	public String initCreationForm() {
		return VIEWS_INCIDENT_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/incidents/new")
	public String processCreationForm(@Valid Incident incident, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the oincident.");
			return VIEWS_INCIDENT_CREATE_OR_UPDATE_FORM;
		}

		this.incidents.save(incident);
		redirectAttributes.addFlashAttribute("message", "New Incident reported");
		return "redirect:/incidents/" + incident.getId();
	}

	@GetMapping("/incidents/find")
	public String initFindForm() {
		return "incidents/findIncidents";
	}

	@GetMapping("/incidents")
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Incident incident, BindingResult result,
								  Model model) {
		// allow parameterless GET request for /incidents to return all records
		if (incident.getRefNumber() == null) {
			incident.setRefNumber(""); // empty string signifies broadest possible search
		}

		// find owners by last name
		Page<Incident> incidentsResults = findPaginatedForIncidentRefNumbers(page, incident.getRefNumber());
		if (incidentsResults.isEmpty()) {
			// no owners found
			result.rejectValue("refNumber", "notFound", "not found");
			return "incidents/findIncidents";
		}

		if (incidentsResults.getTotalElements() == 1) {
			// 1 owner found
			incident = incidentsResults.iterator().next();
			return "redirect:/incidents/" + incident.getId();
		}

		return addPaginationModel(page, model, incidentsResults);
	}

	private String addPaginationModel(int page, Model model, Page<Incident> paginated) {
		List<Incident> listIncidents = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listIncidents);
		return "owners/ownersList";
	}


	private Page<Incident> findPaginatedForIncidentRefNumbers(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return incidents.findIncidentByRefNumberStartingWith(lastname, pageable);
	}

	@GetMapping("/incidents/{incidentId}/edit")
	public String initUpdateIncidentForm() {
		return VIEWS_INCIDENT_CREATE_OR_UPDATE_FORM;
	}

	@GetMapping("/incidents/{incidentId}")
	public ModelAndView showOwner(@PathVariable("incidentId") int incidentId) {
		ModelAndView mav = new ModelAndView("incidents/incidentsDetails");
		Optional<Incident> optionalOwner = this.incidents.findById(incidentId);
		Incident owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
			"Owner not found with id: " + incidentId + ". Please ensure the ID is correct "));
		mav.addObject(owner);
		return mav;
	}


}
