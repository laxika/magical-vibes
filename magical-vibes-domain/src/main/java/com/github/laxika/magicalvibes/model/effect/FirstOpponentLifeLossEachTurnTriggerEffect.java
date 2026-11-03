package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Trigger descriptor for "whenever an opponent loses life for the first time during each of their
 * turns". The trigger is tracked independently for each opponent of the source permanent. Not authored on a card: the
 * legacy view ({@code TriggeredAbility.legacyEffects()}) builds it from
 * {@code Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn()} and the ability's effects.
 *
 * @param resolvedEffects effects to put on the stack when the trigger fires
 */
public record FirstOpponentLifeLossEachTurnTriggerEffect(List<CardEffect> resolvedEffects)
        implements CardEffect {
}
