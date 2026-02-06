package com.github.laxika.magicalvibes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_name", nullable = false, length = 100)
    private String gameName;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private User createdBy;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GamePlayer> players = new ArrayList<>();

    public Game() {
    }

    public Game(String gameName, Long createdByUserId, String status) {
        this.gameName = gameName;
        this.createdByUserId = createdByUserId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}
