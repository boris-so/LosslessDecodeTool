package ru.boris.music.model.settings;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "Formats")
public class Format
{
    private int id;
    private String frm;
    private Collection<Operation> operationsById;


    @Id @Column(name = "id", nullable = false) @GeneratedValue(strategy = GenerationType.TABLE)
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }


    public void setFrm(String frm)
    {
        this.frm = frm;
    }


    @Override public int hashCode()
    {
        int result = id;
        result = 31 * result + (frm != null ? frm.hashCode() : 0);
        return result;
    }

    @Override public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Format format = (Format) o;

        if (id != format.id) return false;
        if (frm != null ? !frm.equals(format.frm) : format.frm != null) return false;

        return true;
    }


    @OneToMany
    @JoinColumn(name = "fk_frm", referencedColumnName = "id", nullable = false)
    public Collection<Operation> getOperationsById()
    {
        return operationsById;
    }
    public void setOperationsById(Collection<Operation> operationsesById)
    {
        this.operationsById = operationsesById;
    }
}
