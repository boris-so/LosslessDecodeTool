package ru.boris.music.model.settings;

import javax.persistence.*;

@Entity
@Table (name = "Operations")
public class Operation
{

    private int id;
    private String cmd;
    private Comment commentById;
    private Format formatByFkFrm;

    @Id @Column(name = "id", nullable = false) @GeneratedValue(strategy = GenerationType.TABLE)
    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }

    @Basic @Column(name = "cmd", nullable = false, length = 0)
    public String getCmd()
    {
        return cmd;
    }
    public void setCmd(String cmd)
    {
        this.cmd = cmd;
    }


    @Override public int hashCode()
    {
        int result = id;
        result = 31 * result + (cmd != null ? cmd.hashCode() : 0);
        return result;
    }
    @Override public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operation that = (Operation) o;

        if (id != that.id) return false;
        if (cmd != null ? !cmd.equals(that.cmd) : that.cmd != null) return false;

        return true;
    }


    @OneToOne @JoinColumn(name = "id", referencedColumnName = "fk_operations")
    public Comment getCommentById()
    {
        return this.commentById;
    }
    public void setCommentById(Comment commentById)
    {
        this.commentById = commentById;
    }

    @ManyToOne @JoinColumn
            (
                    name = "fk_frm",
                    referencedColumnName = "id",
                    nullable = false,
                    insertable = false,
                    updatable = false
            )
    public Format getFormatByFkFrm()
    {
        return formatByFkFrm;
    }
    public void setFormatByFkFrm(Format formatByFkFrm)
    {
        this.formatByFkFrm = formatByFkFrm;
    }
}
