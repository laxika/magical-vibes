package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Dungeon;

/** Advances the controller's venture marker, starting with Lost Mine of Phandelver when needed. */
public record VentureIntoDungeonEffect(Dungeon dungeon) implements CardEffect {

    public VentureIntoDungeonEffect() {
        this(Dungeon.LOST_MINE_OF_PHANDELVER);
    }
}
