package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * One triggered ability that triggered, as the collectors hand it to a watch's handler
 * ({@link TriggerCollectorRegistry#dispatch}): its source, its controller and the effects it puts on the stack as one
 * object (CR 603.3), in order. A handler routes the ability whole: to its target choice with every effect, or onto the
 * stack as one entry of them.
 *
 * @param gameData     the current game state
 * @param permanent    the permanent whose ability triggered, or {@code null} for an ability of a card in exile, a
 *                     graveyard or the command zone
 * @param controllerId the controller of that permanent/source card
 * @param effects      the ability's effects, in the order they resolve, as the collector read them (an intervening
 *                     "if" wrapped around each, CR 603.4; a "you may" is the effect itself, asked as the ability
 *                     resolves, CR 603.5); an engine-made ability of one effect has that effect alone
 * @param sourceCard   the card whose ability triggered
 * @param markSourceOncePerTurnOnAcceptance whether accepting this trigger's may ability consumes
 *                                           the source's once-per-turn allowance
 * @param triggersOnlyOnceEachTurn whether the ability "triggers only once each turn" ({@code
 *                                 TriggerLimit.ONCE_EACH_TURN}), which nothing can make trigger additional times
 * @param registeringPermanentId for an ability a resolving ability registered, the permanent that was that ability's
 *                               source and so this one's (CR 603.7e), which may have left the battlefield; {@code null}
 *                               otherwise
 */
public record TriggerMatchContext(
        GameData gameData,
        Permanent permanent,
        UUID controllerId,
        List<CardEffect> effects,
        Card sourceCard,
        PlanarObject sourcePlanarObject,
        boolean markSourceOncePerTurnOnAcceptance,
        boolean triggersOnlyOnceEachTurn,
        UUID registeringPermanentId
) {

    public TriggerMatchContext {
        effects = List.copyOf(effects);
    }

    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId, List<CardEffect> effects,
            Card sourceCard, PlanarObject sourcePlanarObject, boolean markSourceOncePerTurnOnAcceptance,
            boolean triggersOnlyOnceEachTurn) {
        this(gameData, permanent, controllerId, effects, sourceCard, sourcePlanarObject,
                markSourceOncePerTurnOnAcceptance, triggersOnlyOnceEachTurn, null);
    }

    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId, List<CardEffect> effects,
            Card sourceCard, PlanarObject sourcePlanarObject, boolean markSourceOncePerTurnOnAcceptance) {
        this(gameData, permanent, controllerId, effects, sourceCard, sourcePlanarObject,
                markSourceOncePerTurnOnAcceptance, false);
    }

    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId, List<CardEffect> effects) {
        this(gameData, permanent, controllerId, effects, permanent != null ? permanent.getCard() : null, null, false);
    }

    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId, List<CardEffect> effects,
            Card sourceCard) {
        this(gameData, permanent, controllerId, effects, sourceCard, null, false);
    }

    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId, List<CardEffect> effects,
            Card sourceCard, PlanarObject sourcePlanarObject) {
        this(gameData, permanent, controllerId, effects, sourceCard, sourcePlanarObject, false);
    }

    /**
     * An ability of the one effect {@code effect}, with the other components as
     * {@link #TriggerMatchContext(GameData, Permanent, UUID, List, Card, PlanarObject, boolean, boolean)} takes them.
     */
    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId, CardEffect effect,
            Card sourceCard, PlanarObject sourcePlanarObject, boolean markSourceOncePerTurnOnAcceptance,
            boolean triggersOnlyOnceEachTurn) {
        this(gameData, permanent, controllerId, List.of(effect), sourceCard, sourcePlanarObject,
                markSourceOncePerTurnOnAcceptance, triggersOnlyOnceEachTurn, null);
    }

    /** An ability of the one effect {@code effect}. */
    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId, CardEffect effect) {
        this(gameData, permanent, controllerId, List.of(effect));
    }

    /** An ability of the one effect {@code effect} of {@code sourceCard}. */
    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId, CardEffect effect,
            Card sourceCard) {
        this(gameData, permanent, controllerId, List.of(effect), sourceCard);
    }

    /**
     * An ability of the one effect {@code effect} of {@code sourceCard}, a face-up plane's when
     * {@code sourcePlanarObject}.
     */
    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId, CardEffect effect,
            Card sourceCard, PlanarObject sourcePlanarObject) {
        this(gameData, permanent, controllerId, List.of(effect), sourceCard, sourcePlanarObject);
    }

    /**
     * The object "a card exiled with this" names: the source permanent, else the face-up plane, else the permanent that
     * registered the ability; {@code null} for none. A card cast or played from exile names the object it was exiled
     * with in its event, since it has left exile by then.
     */
    public UUID exiledWithSourceId() {
        return permanent != null ? permanent.getId()
                : sourcePlanarObject != null ? sourcePlanarObject.getId() : registeringPermanentId;
    }

    /** The ability's effects as a new mutable list, for the one stack entry or target choice that carries them. */
    public List<CardEffect> stackEffects() {
        return new ArrayList<>(effects);
    }

    /** Whether any of the ability's effects admits a target of {@code kind}. */
    public boolean admits(TargetPredicate.Kind kind) {
        return effects.stream().anyMatch(effect -> effect.targetSpec().admits(kind));
    }

    /** Whether any of the ability's effects targets "any target": one effect that admits a player and a permanent. */
    public boolean admitsAnyTarget() {
        return effects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                && effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
    }

    /** Whether any of the ability's effects declares a target ({@code TargetSpec.declaredTarget()}). */
    public boolean declaresTarget() {
        return effects.stream().anyMatch(effect -> effect.targetSpec().declaredTarget() != null);
    }

    /** Whether any of the ability's effects targets a spell on the stack. */
    public boolean targetsSpellOnStack() {
        return effects.stream().anyMatch(EffectResolution::targetsSpellOnStack);
    }

    /** Whether the ability targets: a player, a permanent, a graveyard card or a spell. */
    public boolean targets() {
        return effects.stream().anyMatch(TriggerMatchContext::targets);
    }

    /**
     * The ability's first effect that targets, whose target spec and target group the ability's target choice reads,
     * or {@code null} when none does.
     */
    public CardEffect targetingEffect() {
        return effects.stream().filter(TriggerMatchContext::targets).findFirst().orElse(null);
    }

    /** Whether every targeting effect of the ability has an optional target ("up to one"). */
    public boolean targetIsOptional() {
        return effects.stream().filter(TriggerMatchContext::targets).allMatch(CardEffect::hasOptionalTarget);
    }

    private static boolean targets(CardEffect effect) {
        return effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                || EffectResolution.targetsSpellOnStack(effect);
    }

    /** The first of the ability's effects that is a {@code type}, or {@code null}. */
    public <T extends CardEffect> T effectOf(Class<T> type) {
        for (CardEffect effect : effects) {
            if (type.isInstance(effect)) {
                return type.cast(effect);
            }
        }
        return null;
    }

    /**
     * What {@code modesOf} reads from the ability's one effect when it is a modal ability (a trigger-time "choose
     * one"), whose modes are the whole ability, or {@code null} when it isn't: {@code modesOf} gives {@code null} for
     * an effect that isn't modal. A modal written beside other effects fails loudly instead of being split.
     */
    public <T> T modal(Function<CardEffect, T> modesOf) {
        T modes = null;
        for (CardEffect effect : effects) {
            T read = modesOf.apply(effect);
            if (read != null) {
                modes = read;
            }
        }
        if (modes != null && effects.size() > 1) {
            throw new IllegalStateException("A modal triggered ability is one effect; this one has others: " + effects);
        }
        return modes;
    }

    /** This ability with {@code effects} as its effects. */
    public TriggerMatchContext withEffects(List<CardEffect> effects) {
        return new TriggerMatchContext(gameData, permanent, controllerId, effects, sourceCard, sourcePlanarObject,
                markSourceOncePerTurnOnAcceptance, triggersOnlyOnceEachTurn, registeringPermanentId);
    }
}
