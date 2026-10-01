package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Dynamically grants a static effect to creatures matching the optional filter until end of
 * turn. The affected set is evaluated continuously, so later creature entries are included.
 *
 * @param staticEffect the static effect granted to matching creatures
 * @param filter optional additional filter applied after the creature check
 */
public record GrantStaticEffectToAllCreaturesUntilEndOfTurnEffect(
        CardEffect staticEffect, PermanentPredicate filter) implements CardEffect {

    public GrantStaticEffectToAllCreaturesUntilEndOfTurnEffect(CardEffect staticEffect) {
        this(staticEffect, null);
    }
}
