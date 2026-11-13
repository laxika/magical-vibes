package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;

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
        implements CardEffect {

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
    public GraveyardCardChoosingEffect graveyardCardChoice() {
        return wrapped.graveyardCardChoice();
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec wrappedSpec = wrapped.targetSpec();
        return wrappedSpec != TargetSpec.NONE || elseEffect == null
                ? wrappedSpec
                : elseEffect.targetSpec();
    }

    /** A static "you may" functions from the zones of the ability it offers (an opening-hand action: the hand). */
    @Override
    public AbilityZones staticAbilityZones() {
        return wrapped.staticAbilityZones();
    }

    /** "That player may … target … of their choice": the active player who answers also chooses the target. */
    @Override
    public boolean targetChosenByAnOpponent() {
        return wrapped.targetChosenByAnOpponent();
    }

    @Override
    public boolean targetChosenByActivePlayer() {
        return choicePlayer == MayChoicePlayer.ACTIVE_PLAYER || wrapped.targetChosenByActivePlayer();
    }

    /** The triggering permanent's controller who makes the choice also chooses its target (Pandemonium). */
    @Override
    public boolean targetChosenByTriggeringPermanentController() {
        return choicePlayer == MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER
                || wrapped.targetChosenByTriggeringPermanentController();
    }

    @Override
    public boolean targetsChosenByReflexiveAbility() {
        return wrapped.targetsChosenByReflexiveAbility();
    }

    @Override
    public boolean resolvesWhenTargetIllegal() {
        return wrapped.resolvesWhenTargetIllegal();
    }
}
