package com.sparta.springcore.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ApiUseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long totalTime;

    public ApiUseTime(User user, Long totalTime){
        this.user = user;
        this.totalTime = totalTime;
    }

    public void addUseTime(long runTime) {
        this.totalTime += runTime;
    }
}
