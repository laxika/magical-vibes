package com.github.laxika.magicalvibes.model.effect;

/** Until the controller's next turn, the controller's opponents can't cast spells with the triggering spell's mana value. */
public record OpponentsCantCastSpellsWithManaValueUntilNextTurnEffect()
        implements TriggeringSpellManaValueEffect {
}
