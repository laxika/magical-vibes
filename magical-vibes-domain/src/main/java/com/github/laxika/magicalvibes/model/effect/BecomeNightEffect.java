package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Changes the game's day/night designation (CR 732.1): "it becomes night" (the no-argument form), "it becomes day"
 * ({@link #day()}), or "if it's night, it becomes day; otherwise, it becomes night" ({@link #toggle()}, so a game with
 * neither designation becomes night). A designation the game already has doesn't change, and nothing that watches a
 * change happens then.
 *
 * @param change which designation the game gets
 */
public record BecomeNightEffect(Change change) implements CardEffect {

    /** The designation a {@link BecomeNightEffect} gives the game. */
    public enum Change {
        /** "It becomes night." */
        NIGHT,
        /** "It becomes day." */
        DAY,
        /** "If it's night, it becomes day. Otherwise, it becomes night." */
        TOGGLE
    }

    public BecomeNightEffect {
        Objects.requireNonNull(change, "change");
    }

    /** "It becomes night." */
    public BecomeNightEffect() {
        this(Change.NIGHT);
    }

    /** "It becomes day." */
    public static BecomeNightEffect day() {
        return new BecomeNightEffect(Change.DAY);
    }

    /** "If it's night, it becomes day. Otherwise, it becomes night." */
    public static BecomeNightEffect toggle() {
        return new BecomeNightEffect(Change.TOGGLE);
    }
}
