package com.github.laxika.magicalvibes.model.effect;

/**
 * Until the controller's next turn, the controller's opponents can't cast spells with the triggering spell's mana value.
 *
 * @param manaValue the mana value the resolution fixed, carried by the floating effect it made; {@code null} on the
 *                  effect a card is built with
 */
public record OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect(Integer manaValue)
        implements CardEffect {

    public OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect() {
        this(null);
    }
}
