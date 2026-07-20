package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: spells with the same name as the card name chosen by the source permanent
 * (tracked via {@code permanent.chosenName}) can't be cast. When {@code opponentsOnly} is
 * {@code false} the restriction is symmetric — no player can cast such spells (Nevermore).
 * When {@code true} only the controller's opponents are restricted (Gideon's Intervention).
 */
public record SpellsWithChosenNameCantBeCastEffect(boolean opponentsOnly) implements CardEffect {

    public SpellsWithChosenNameCantBeCastEffect() {
        this(false);
    }
}
