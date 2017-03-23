package ru.boris.music.model.settings;

import javax.persistence.*;

@Entity
@Table(name = "Comments")
public class Comment
{
    private int fkOperations;
    private String comment;
    private Operation operationsByFkOperation;


    @Id @Column(name = "fk_operations", nullable = false)
    public int getFkOperations()
    {
        return fkOperations;
    }
    public void setFkOperations(int fkOperations)
    {
        this.fkOperations = fkOperations;
    }

    @Basic @Column(name = "comment", nullable = true, length = 0)
    public String getComment()
    {
        return comment;
    }
    public void setComment(String comment)
    {
        this.comment = comment;
    }


    @Override public int hashCode()
    {
        int result = fkOperations;
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        return result;
    }
    @Override public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Comment comment = (Comment) o;

        return fkOperations == comment.fkOperations && (this.comment != null ? this.comment
                .equals(comment.comment) : comment.comment == null);

    }


    @OneToOne @JoinColumn(name = "fk_operations", referencedColumnName = "id", nullable = false)
    public Operation getOperationsByFkOperation()
    {
        return operationsByFkOperation;
    }
    public void setOperationsByFkOperation(Operation operationsByFkOperation)
    {
        this.operationsByFkOperation = operationsByFkOperation;
    }
}
