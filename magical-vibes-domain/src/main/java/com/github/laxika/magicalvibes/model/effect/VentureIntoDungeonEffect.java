package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Dungeon;

/** Advances the controller's venture marker, choosing a dungeon when no specific dungeon is required. */
public record VentureIntoDungeonEffect(Dungeon dungeon) implements CardEffect {

    public VentureIntoDungeonEffect() {
        this(null);
    }
}
