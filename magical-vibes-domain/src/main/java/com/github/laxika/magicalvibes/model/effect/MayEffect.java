package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * "You may [wrapped]" — the choice is made at resolution time (by the controller by default).
 *
 * @param wrapped      the effect resolved when the choosing player accepts
 * @param prompt       the accept/decline prompt text
 * @param elseEffect   optional "if you don't, [effect]" half resolved when the choosing player declines
 *                   (Petals of Insight's "Otherwise, draw three cards"); {@code null} means
 *                   declining simply does nothing
 * @param choicePlayer identifies the player who makes the choice; {@code DEFENDING_PLAYER} uses
 *                            the player attacked by an attack trigger
 */
public record MayEffect(CardEffect wrapped, String prompt, CardEffect elseEffect, MayChoicePlayer choicePlayer, UUID choicePlayerId)
        implements GrantingPermanentAwareEffect, CombatDamageTriggerContextEffect, CombatDamageDealerAwareEffect,
        TriggeringPermanentSourceEffect, CombatOpponentReferencingEffect,
        SacrificedPermanentManaValueAwareEffect, DyingCreaturePermanentAwareEffect,
        TriggeringPermanentManaValueEffect, TriggeringPermanentEntryExclusionEffect,
        DyingCreatureCountersAwareEffect, LeavingPermanentCountersAwareEffect {

    public MayEffect(CardEffect wrapped, String prompt, CardEffect elseEffect, MayChoicePlayer choicePlayer) {
        this(wrapped, prompt, elseEffect, choicePlayer, null);
    }

    /** A resolution-time decision by an explicitly identified player. */
    public static MayEffect forPlayer(CardEffect wrapped, String prompt, UUID playerId) {
        return new MayEffect(wrapped, prompt, null, MayChoicePlayer.CONTROLLER, playerId);
    }

    public MayEffect(CardEffect wrapped, String prompt, CardEffect elseEffect) {
        this(wrapped, prompt, elseEffect, MayChoicePlayer.CONTROLLER);
    }

    /** Plain "you may" with nothing happening on a decline. */
    public MayEffect(CardEffect wrapped, String prompt) {
        this(wrapped, prompt, null, MayChoicePlayer.CONTROLLER);
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec wrappedSpec = wrapped.targetSpec();
        return wrappedSpec != TargetSpec.NONE || elseEffect == null
                ? wrappedSpec
                : elseEffect.targetSpec();
    }

    @Override
    public boolean resolvesWhenTargetIllegal() {
        return wrapped.resolvesWhenTargetIllegal();
    }

    @Override
    public boolean usesEnteringPermanentReference() {
        return wrapped.usesEnteringPermanentReference();
    }

    @Override
    public boolean referencesCombatOpponent() {
        return referencesCombatOpponent(wrapped) || referencesCombatOpponent(elseEffect);
    }

    private static boolean referencesCombatOpponent(CardEffect effect) {
        return effect instanceof CombatOpponentReferencingEffect combatOpponent
                && combatOpponent.referencesCombatOpponent();
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return wrapped instanceof CombatDamageTriggerContextEffect contextEffect
                ? contextEffect.combatDamageTriggerContext()
                : null;
    }

    @Override
    public CardEffect withCombatDamageDealerIds(List<UUID> dealerIds) {
        CardEffect boundWrapped = wrapped instanceof CombatDamageDealerAwareEffect aware
                ? aware.withCombatDamageDealerIds(dealerIds)
                : wrapped;
        return new MayEffect(boundWrapped, prompt, elseEffect, choicePlayer, choicePlayerId);
    }

    @Override
    public CardEffect withGrantingPermanentId(UUID permanentId) {
        CardEffect boundWrapped = wrapped instanceof GrantingPermanentAwareEffect aware
                ? aware.withGrantingPermanentId(permanentId)
                : wrapped;
        CardEffect boundElse = elseEffect instanceof GrantingPermanentAwareEffect aware
                ? aware.withGrantingPermanentId(permanentId)
                : elseEffect;
        return new MayEffect(boundWrapped, prompt, boundElse, choicePlayer, choicePlayerId);
    }

    @Override
    public boolean sourceIsTriggeringPermanent() {
        return wrapped instanceof TriggeringPermanentSourceEffect source
                && source.sourceIsTriggeringPermanent();
    }

    @Override
    public boolean usesTriggeringPermanentManaValue() {
        return usesTriggeringPermanentManaValue(wrapped) || usesTriggeringPermanentManaValue(elseEffect);
    }

    private static boolean usesTriggeringPermanentManaValue(CardEffect effect) {
        return effect instanceof TriggeringPermanentManaValueEffect valueEffect
                && valueEffect.usesTriggeringPermanentManaValue();
    }

    @Override
    public boolean suppressesTriggeringPermanentEntry() {
        return suppressesTriggeringPermanentEntry(wrapped) || suppressesTriggeringPermanentEntry(elseEffect);
    }

    private static boolean suppressesTriggeringPermanentEntry(CardEffect effect) {
        return effect instanceof TriggeringPermanentEntryExclusionEffect exclusion
                && exclusion.suppressesTriggeringPermanentEntry();
    }

    @Override
    public MayEffect boundToSacrificedPermanentManaValue(int manaValue) {
        CardEffect boundWrapped = wrapped instanceof SacrificedPermanentManaValueAwareEffect aware
                ? aware.boundToSacrificedPermanentManaValue(manaValue)
                : wrapped;
        return new MayEffect(boundWrapped, prompt, elseEffect, choicePlayer, choicePlayerId);
    }

    @Override
    public CardEffect boundToDyingCreature(com.github.laxika.magicalvibes.model.Permanent dyingCreature) {
        CardEffect boundWrapped = wrapped instanceof DyingCreaturePermanentAwareEffect aware
                ? aware.boundToDyingCreature(dyingCreature)
                : wrapped;
        CardEffect boundElse = elseEffect instanceof DyingCreaturePermanentAwareEffect aware
                ? aware.boundToDyingCreature(dyingCreature)
                : elseEffect;
        if (boundWrapped == wrapped && boundElse == elseEffect) {
            return this;
        }
        return new MayEffect(boundWrapped, prompt, boundElse, choicePlayer, choicePlayerId);
    }

    @Override
    public CardEffect boundToDyingCreatureCounters(Map<CounterType, Integer> counters) {
        CardEffect boundWrapped = wrapped instanceof DyingCreatureCountersAwareEffect aware
                ? aware.boundToDyingCreatureCounters(counters)
                : wrapped;
        CardEffect boundElse = elseEffect instanceof DyingCreatureCountersAwareEffect aware
                ? aware.boundToDyingCreatureCounters(counters)
                : elseEffect;
        if (boundWrapped == wrapped && boundElse == elseEffect) {
            return this;
        }
        return new MayEffect(boundWrapped, prompt, boundElse, choicePlayer, choicePlayerId);
    }

    @Override
    public CardEffect boundToLeavingPermanentCounters(Map<CounterType, Integer> counters) {
        CardEffect boundWrapped = wrapped instanceof LeavingPermanentCountersAwareEffect aware
                ? aware.boundToLeavingPermanentCounters(counters)
                : wrapped;
        CardEffect boundElse = elseEffect instanceof LeavingPermanentCountersAwareEffect aware
                ? aware.boundToLeavingPermanentCounters(counters)
                : elseEffect;
        if (boundWrapped == wrapped && boundElse == elseEffect) {
            return this;
        }
        return new MayEffect(boundWrapped, prompt, boundElse, choicePlayer, choicePlayerId);
    }
}
