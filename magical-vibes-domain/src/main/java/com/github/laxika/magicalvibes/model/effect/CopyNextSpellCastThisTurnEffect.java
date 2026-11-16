package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

/**
 * Creates the delayed triggered ability "When you next cast a [{@code spellFilter}] spell this turn, copy it. You may
 * choose new targets for the copy." ({@link #delayedAbility}), which triggers only the next time it applies and ends
 * with the turn if it hasn't (CR 603.7b). A copy of a permanent spell becomes a token (CR 608.3f), without
 * {@code removedSupertypes} ("except the copy isn't legendary").
 *
 * @param spellFilter       which spell it copies; {@code null} for any spell
 * @param removedSupertypes supertypes the copy doesn't have
 */
public record CopyNextSpellCastThisTurnEffect(
        CardPredicate spellFilter,
        Set<CardSupertype> removedSupertypes
) implements CardEffect {

    public CopyNextSpellCastThisTurnEffect() {
        this(null, Set.of());
    }

    public CopyNextSpellCastThisTurnEffect {
        removedSupertypes = removedSupertypes == null ? Set.of() : Set.copyOf(removedSupertypes);
    }

    /** The delayed triggered ability this creates. */
    public TriggeredAbility delayedAbility() {
        CopyTriggeringSpellEffect copy = new CopyTriggeringSpellEffect().permanentSpellCopiesAreTokens()
                .withoutSupertypes(removedSupertypes);
        return new TriggeredAbility(spellFilter == null ? Triggers.youCastSpell()
                : Triggers.youCastSpell().withFilter(spellFilter), copy);
    }
}
