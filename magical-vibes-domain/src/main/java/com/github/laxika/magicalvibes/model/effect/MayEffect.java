package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.MayChoicePlayer;

import java.util.List;
import java.util.UUID;

/**
 * "You may [wrapped]" — the choice is made at resolution time (by the controller by default).
 *
 * @param wrapped      the effect resolved when the choosing player accepts
 * @param prompt       the accept/decline prompt text
 * @param elseEffect   optional "if you don't, [effect]" half resolved when the choosing player declines
 *                   (Petals of Insight's "Otherwise, draw three cards"); {@code null} means
 *                   declining simply does nothing
 * @param choicePlayer identifies the player who makes the choice
 */
public record MayEffect(CardEffect wrapped, String prompt, CardEffect elseEffect, MayChoicePlayer choicePlayer)
        implements CombatDamageTriggerContextEffect, CombatDamageDealerAwareEffect {

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
        return new MayEffect(boundWrapped, prompt, elseEffect, choicePlayer);
    }
}
