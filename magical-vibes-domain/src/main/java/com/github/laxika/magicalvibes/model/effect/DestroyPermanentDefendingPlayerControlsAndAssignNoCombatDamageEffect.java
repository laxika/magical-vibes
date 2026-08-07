package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Destroy target [filter] defending player controls and this creature assigns no combat damage
 * this turn." (Goblin Vandal, behind a {@link MayPayManaEffect}.)
 *
 * <p>The destroy counterpart of
 * {@link GainControlOfPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect}: use it on an
 * {@code ON_ATTACKS_UNBLOCKED} trigger, whose stack entry carries the defending player as
 * {@code targetId} and the attacking creature as {@code sourcePermanentId}. On resolution the
 * controller picks one permanent the defending player controls that matches {@code filter}; it is
 * destroyed and — only when one is actually chosen — the source assigns no combat damage this turn.
 *
 * @param filter which of the defending player's permanents may be chosen
 * @param choiceNoun singular noun used in the choice prompt and log (e.g. {@code "artifact"})
 */
public record DestroyPermanentDefendingPlayerControlsAndAssignNoCombatDamageEffect(
        PermanentPredicate filter, String choiceNoun) implements RemovalEffect {

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
