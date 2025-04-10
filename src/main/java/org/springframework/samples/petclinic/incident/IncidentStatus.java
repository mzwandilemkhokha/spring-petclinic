package org.springframework.samples.petclinic.incident;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.samples.petclinic.model.NamedEntity;

@Entity
@Table(name = "incident_status")
public class IncidentStatus extends NamedEntity
{
}
