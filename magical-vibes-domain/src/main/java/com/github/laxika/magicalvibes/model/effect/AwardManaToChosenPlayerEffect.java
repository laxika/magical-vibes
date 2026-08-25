package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * "Choose a player. That player adds {@code amount} mana of {@code color}." This is a mana
 * ability (CR 605.1a): it doesn't use the stack and can't be responded to. Choosing a player
 * is not targeting, so the engine resolves it inline during mana-ability resolution — it begins
 * a player-choice interaction and routes the produced mana into the chosen player's pool
 * (which may be an opponent's). Implements {@link ManaProducingEffect} so the engine treats the
 * ability as a mana ability. Used by Valleymaker ("{T}, Sacrifice a Forest: Choose a player.
 * That player adds {G}{G}{G}.").
 */
public record AwardManaToChosenPlayerEffect(ManaColor color, int amount, boolean anyColor)
        implements ManaProducingEffect {

    public AwardManaToChosenPlayerEffect(ManaColor color, int amount) {
        this(color, amount, false);
    }

    /** The chosen player chooses the color of the mana they receive. */
    public static AwardManaToChosenPlayerEffect anyColor(int amount) {
        return new AwardManaToChosenPlayerEffect(null, amount, true);
    }
}
