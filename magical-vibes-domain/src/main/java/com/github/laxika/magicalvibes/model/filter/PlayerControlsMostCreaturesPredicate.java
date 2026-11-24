package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a player who controls the most creatures, a player tied for the most included ("each player who controls the
 * most creatures investigates", No Witnesses), or the most creatures matching {@code creatureFilter} ("the player who
 * controls the most Wizards", Thoughtbound Primoc, the same filter its intervening "if" counts).
 *
 * @param creatureFilter which permanents count, judged for the effect's source; {@code null} for every creature
 */
public record PlayerControlsMostCreaturesPredicate(PermanentPredicate creatureFilter) implements PlayerPredicate {

    public PlayerControlsMostCreaturesPredicate() {
        this(null);
    }
}
