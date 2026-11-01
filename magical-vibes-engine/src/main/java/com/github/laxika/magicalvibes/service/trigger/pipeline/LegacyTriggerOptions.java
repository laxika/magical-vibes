package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfDyingSourceHadCounterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardAndShuffleTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreateTokenCopyOfDyingSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardWithOneFewerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledFromBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledWhileActivatingCraftAbilityEffect;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Reads the trigger-only wrapper effects of an ability still held in a slot, the way the slot's collector and its
 * handlers read them, so the trigger pipeline can tell what that ability would trigger. The trigger pipeline reads
 * slot abilities through this one adapter until step 14 of effect-slot-refactor.md turns these wrappers and
 * conditions into {@code TriggerOptions} and trigger filters. What it reads:
 *
 * <ul>
 *   <li>The creature-death conditionals and the death intervening "if", through the collector's own code.</li>
 *   <li>Intervening "if"s written as effects: "if it had a counter on it" and its relatives.</li>
 *   <li>Filters written as effects: "into your graveyard", "when this is exiled", "while you activate its craft
 *       ability".</li>
 *   <li>{@code OncePerTurnTriggerEffect} is taken off but its limit isn't checked. The pipeline matches a batch
 *       after the collectors ran for it, and a collector has already used the limit up by then.</li>
 * </ul>
 */
@Service
public class LegacyTriggerOptions {

    private final TriggerCollectionService triggerCollectionService;
    private final ConditionEvaluationService conditionEvaluationService;

    public LegacyTriggerOptions(@Lazy TriggerCollectionService triggerCollectionService,
                                @Lazy ConditionEvaluationService conditionEvaluationService) {
        this.triggerCollectionService = triggerCollectionService;
        this.conditionEvaluationService = conditionEvaluationService;
    }

    /**
     * What {@code effect}, a registration of {@code candidate}'s ability in {@code slot}, puts on the stack for
     * {@code event}: the effect with its trigger-only wrappers taken off, or {@code null} when they keep it from
     * triggering. {@link LegacySlotTriggers#of(EffectSlot, CardEffect)} has already matched the event.
     */
    public CardEffect resolve(GameData gameData, EffectSlot slot, ZoneChangeEvent event, TriggerCandidate candidate,
                              CardEffect effect) {
        CardEffect unlimited = effect instanceof OncePerTurnTriggerEffect once ? once.wrapped() : effect;
        return switch (slot) {
            case ON_DEATH -> death(gameData, event, candidate, unlimited);
            case ON_SELF_LEAVES_BATTLEFIELD -> leavesTheBattlefield(gameData, event, candidate, unlimited);
            default -> unlimited;
        };
    }

    private CardEffect death(GameData gameData, ZoneChangeEvent event, TriggerCandidate candidate,
                             CardEffect effect) {
        if (effect.onlyTriggersOnSacrifice()) {
            return effect;
        }
        CardEffect resolved = triggerCollectionService.legacyDeathTriggerEffect(gameData, candidate.sourceCard(),
                candidate.sourceAsPermanent(), candidate.controllerId(), hasType(event, CardType.CREATURE),
                hasType(event, CardType.LAND), effect);
        if (resolved == null) {
            return null;
        }
        Permanent lastKnown = event.permanent();
        boolean passes = switch (resolved) {
            case DrawCardIfDyingSourceHadCounterEffect draw -> counters(lastKnown, draw.counterType()) >= 1;
            case RegisterDelayedCreateTokenCopyOfDyingSourceEffect copy ->
                    counters(lastKnown, CounterType.PLUS_ONE_PLUS_ONE) >= copy.minimumCounterCount();
            case RegisterDelayedSelfReturnFromGraveyardWithOneFewerCounterEffect returnEffect ->
                    counters(lastKnown, returnEffect.counterType()) >= 1;
            case MoveDyingSourceCountersToTargetCreatureEffect move ->
                    !move.requiresCounters() || hadCounters(lastKnown);
            case CreateTokenWithDyingSourceCountersEffect ignored -> hadCounters(lastKnown);
            // "When this dies during combat".
            case DealDamageToBlockedAttackersOnDeathEffect ignored -> inCombat(gameData);
            // "When this is put into your graveyard from the battlefield": a card going to another player's
            // graveyard doesn't trigger it.
            case RegisterDelayedLoseLifeAndReturnFromGraveyardToHandEffect ignored ->
                    candidate.controllerId().equals(event.ownerId());
            case ExileSourceCardFromGraveyardAndShuffleTopCardsEffect ignored ->
                    candidate.controllerId().equals(event.ownerId());
            default -> true;
        };
        return passes ? resolved : null;
    }

    private CardEffect leavesTheBattlefield(GameData gameData, ZoneChangeEvent event, TriggerCandidate candidate,
                                            CardEffect effect) {
        boolean passes = switch (effect) {
            case SelfExiledFromBattlefieldEffect exiled -> event.to() == Zone.EXILE
                    && interveningIfMet(gameData, candidate, exiled.wrapped());
            case SelfExiledWhileActivatingCraftAbilityEffect ignored ->
                    event.to() == Zone.EXILE && event.cause() == ZoneChangeCause.CRAFT;
            // The leave collector checks only a trigger-time condition; an intervening "if" waits for resolution.
            case ConditionalEffect conditional -> !conditional.triggerTimeOnly()
                    || conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forPermanent(candidate.sourceAsPermanent(), candidate.controllerId()));
            default -> true;
        };
        return passes ? effect : null;
    }

    private boolean interveningIfMet(GameData gameData, TriggerCandidate candidate, CardEffect effect) {
        return conditionEvaluationService.isInterveningIfMet(gameData, effect, candidate.sourceAsPermanent(),
                candidate.controllerId());
    }

    private static boolean hadCounters(Permanent permanent) {
        return permanent != null && permanent.getCounters().values().stream().anyMatch(count -> count > 0);
    }

    private static boolean inCombat(GameData gameData) {
        TurnStep step = gameData.currentStep;
        return step != null && step.ordinal() >= TurnStep.BEGINNING_OF_COMBAT.ordinal()
                && step.ordinal() <= TurnStep.END_OF_COMBAT.ordinal();
    }

    private static int counters(Permanent permanent, CounterType type) {
        return permanent == null ? 0 : permanent.getCounterCount(type);
    }

    /** Whether the moved object had {@code type} as it left: the last-known card holds its effective types. */
    private static boolean hasType(ZoneChangeEvent event, CardType type) {
        Card card = event.permanent() != null ? event.permanent().getCard() : event.card();
        return card.hasType(type);
    }
}
