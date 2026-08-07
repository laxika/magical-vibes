package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

import java.util.Set;

/**
 * Spell-cast trigger descriptor: put {@code amount} +1/+1 counters on the source permanent whenever a
 * spell of one of {@code triggerColors} is cast. An empty {@code triggerColors} set means any spell,
 * colorless ones included (Managorger Hydra); {@code onlyOwnSpells} restricts the
 * {@code ON_ANY_PLAYER_CASTS_SPELL} slot to the source's controller.
 */
public record PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
        Set<CardColor> triggerColors,
        int amount,
        boolean onlyOwnSpells
) implements CardEffect {

    /** Whether a cast spell of {@code spellColor} (possibly {@code null} for colorless) fires this trigger. */
    public boolean matchesColor(CardColor spellColor) {
        return triggerColors.isEmpty() || (spellColor != null && triggerColors.contains(spellColor));
    }
}
