package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.CardColor;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDiscardingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentCausedDiscardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.github.laxika.magicalvibes.model.GameLog;
/**
 * Trigger collectors for discard and cycling events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscardTriggerCollectorService {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final DamagePreventionService damagePreventionService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final LifeSupport lifeSupport;
    private final ConditionEvaluationService conditionEvaluationService;

    @CollectsTrigger(value = ConditionalEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    @CollectsTrigger(value = ConditionalEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleConditionalDiscard(TriggerMatchContext match,
            ConditionalEffect trigger, TriggerContext ctx) {
        if (trigger.interveningIf()
                && !conditionEvaluationService.isMet(match.gameData(), trigger.condition(),
                ConditionContext.forPermanent(match.permanent(), match.controllerId()))) {
            return false;
        }
        UUID triggeringPlayerId = ctx instanceof TriggerContext.Discard discard
                ? discard.discardingPlayerId() : null;
        return enqueueDiscardTrigger(match, trigger, "conditional effect", triggeringPlayerId);
    }

    @CollectsTrigger(value = OpponentCausedDiscardTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleOpponentCausedDiscard(TriggerMatchContext match,
            OpponentCausedDiscardTriggerEffect trigger, TriggerContext ctx) {
        if (!match.gameData().discardCausedByOpponent) return false;

        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger.wrapped())),
                null,
                match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on opponent-caused discard", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleDiscardMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        TriggerContext.Discard discard = (TriggerContext.Discard) ctx;
        UUID triggeringCardId = discard.discardedCard() == null ? null : discard.discardedCard().getId();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(may)),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(triggeringCardId);
        if (triggeringCardId != null) {
            entry.setTriggeringCardGraveyardEntryVersion(
                    match.gameData().graveyardEntryVersion(triggeringCardId));
        }
        match.gameData().stack.add(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on discard (may ability)", match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CYCLES)
    private boolean handleCycleMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(may)),
                null,
                match.permanent().getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycling", match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = ReturnToHandEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CYCLES)
    private boolean handleCycleReturnToHand(TriggerMatchContext match, ReturnToHandEffect effect,
                                            TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycling", match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSelfEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CYCLES)
    private boolean handleCyclePutCountersOnSelf(TriggerMatchContext match,
                                                  PutCountersOnSelfEffect effect, TriggerContext ctx) {
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycling", match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = DealDamageToDiscardingPlayerEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleDamageOnDiscard(TriggerMatchContext match,
            DealDamageToDiscardingPlayerEffect trigger, TriggerContext ctx) {
        TriggerContext.Discard dc = (TriggerContext.Discard) ctx;
        Card sourceCard = match.permanent().getCard();
        String cardName = sourceCard.getName();
        int damage = trigger.damage();
        var gameData = match.gameData();
        var discardingPlayerId = dc.discardingPlayerId();
        damage += gameQueryService.getControllerDamageToOpponentBonus(
                gameData, match.controllerId(), discardingPlayerId);

        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " triggers — deals " + damage + " damage to " + gameData.playerIdToName.get(discardingPlayerId) + "."));
        log.info("Game {} - {} triggers on discard, dealing {} damage to {}",
                gameData.id, cardName, damage, gameData.playerIdToName.get(discardingPlayerId));

        CardColor sourceColor = gameQueryService.getEffectiveColor(gameData, match.permanent());
        boolean sourceDamagePrevented = damagePreventionService.isSourceDamagePreventedForPlayer(
                gameData, discardingPlayerId, match.permanent().getId());
        if (sourceDamagePrevented && !gameQueryService.isDamageFromPermanentSourcePrevented(gameData, match.permanent())) {
            damagePreventionService.applySourceDamagePreventionForPlayer(
                    gameData, discardingPlayerId, match.permanent().getId(), damage,
                    gameQueryService.getEffectiveColors(gameData, match.permanent()));
        }
        if (!gameQueryService.isDamageFromPermanentSourcePrevented(gameData, match.permanent())
                && !sourceDamagePrevented
                && !gameQueryService.isDamageFromMatchingSourcePreventedForPlayer(
                gameData, discardingPlayerId, match.permanent())
                && !gameData.isPreventedFromDealingDamage(match.permanent().getId())
                && !damagePreventionService.applyColorDamagePreventionForPlayer(gameData, discardingPlayerId, sourceColor)) {
            damage = damagePreventionService.applyChannelHarmPrevention(
                    gameData, discardingPlayerId,
                    gameQueryService.findPermanentController(gameData, match.permanent().getId()), damage);
            if (damage <= 0) return true;
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, discardingPlayerId, damage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, discardingPlayerId, effectiveDamage, cardName);
            effectiveDamage -= damagePreventionService.applyDamageToControllerAndPutCounterOnSelf(
                    gameData, discardingPlayerId, effectiveDamage);
            if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, discardingPlayerId)) {
                lifeSupport.applyPoisonCounters(gameData, discardingPlayerId, effectiveDamage,
                        cardName, match.controllerId());
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, discardingPlayerId)) {
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(discardingPlayerId) + "'s life total can't change."));
            } else {
                int lifeLoss = effectiveDamage
                        * gameQueryService.opponentLifeLossMultiplier(gameData, discardingPlayerId);
                gameData.playerLifeTotals.put(discardingPlayerId,
                        gameQueryService.lifeAfterDamage(gameData, discardingPlayerId, lifeLoss));
            }
            if (effectiveDamage > 0) {
                gameData.recordDamageToPlayer(discardingPlayerId, effectiveDamage,
                        gameQueryService.isArtifact(gameData, match.permanent()) ? effectiveDamage : 0);
                triggerCollectionService.checkOpponentDealtDamageTriggers(
                        gameData, discardingPlayerId, match.permanent().getId(), effectiveDamage);
            }
        }

        return true;
    }

    @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleDamageToEachOpponentOnDiscard(TriggerMatchContext match,
            DealDamageToPlayersEffect trigger, TriggerContext ctx) {
        if (trigger.recipient() != DamageRecipient.EACH_OPPONENT) return false;

        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on controller discard (damage to each opponent)",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = ChooseModeNotYetChosenThisTurnEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleTurnScopedModalOnDiscard(TriggerMatchContext match,
            ChooseModeNotYetChosenThisTurnEffect trigger, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                sourceCard, match.controllerId(), new ChooseOneEffect(trigger.options()),
                match.permanent().getId(), true));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard (turn-scoped modal)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARD_EVENT)
    private boolean handleDamageToEachOpponentOnDiscardEvent(TriggerMatchContext match,
            DealDamageToPlayersEffect trigger, TriggerContext ctx) {
        if (trigger.recipient() != DamageRecipient.EACH_OPPONENT) return false;

        TriggerContext.DiscardEvent discardEvent = (TriggerContext.DiscardEvent) ctx;
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId());
        entry.setEventValue(discardEvent.discardedCount());
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on controller discard event (damage to each opponent)",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSelfEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARD_EVENT)
    private boolean handlePutCountersOnSelfOnDiscardEvent(TriggerMatchContext match,
            PutCountersOnSelfEffect trigger, TriggerContext ctx) {
        TriggerContext.DiscardEvent discardEvent = (TriggerContext.DiscardEvent) ctx;
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId());
        entry.setEventValue(discardEvent.discardedCount());
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard event (put {} counter(s))", gameData.id,
                sourceCard.getName(), discardEvent.discardedCount());
        return true;
    }

    @CollectsTrigger(value = ExileDiscardedCardFromGraveyardEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    @CollectsTrigger(value = ExileDiscardedCardFromGraveyardEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleExileDiscardedFromGraveyard(TriggerMatchContext match,
            ExileDiscardedCardFromGraveyardEffect trigger, TriggerContext ctx) {
        TriggerContext.Discard dc = (TriggerContext.Discard) ctx;
        var gameData = match.gameData();
        Card discarded = dc.discardedCard();
        if (discarded == null) return false;

        UUID ownerId = dc.discardingPlayerId();
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        // "exile that card from your graveyard" — only if it's actually there (a replacement effect may
        // have sent it elsewhere).
        if (graveyard == null || graveyard.stream().noneMatch(c -> c.getId().equals(discarded.getId()))) {
            return false;
        }

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId());
        entry.setTriggeringCardId(discarded.getId());
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers to exile discarded card {}{}",
                gameData.id, sourceCard.getName(), discarded.getName(),
                trigger.trackWithSource() ? " with the source" : "");
        return true;
    }

    @CollectsTrigger(value = ExileTopCardMayPlayThisTurnEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleExileTopCardMayPlayOnDiscard(TriggerMatchContext match,
            ExileTopCardMayPlayThisTurnEffect trigger, TriggerContext ctx) {
        return enqueueDiscardTrigger(match, trigger, "exile top card and allow play");
    }

    @CollectsTrigger(value = ScryEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleScryOnDiscard(TriggerMatchContext match, ScryEffect trigger, TriggerContext ctx) {
        // "Whenever you cycle or discard another card, scry N." Cycling discards the card (CR 702.29e),
        // so this single controller-discard trigger fires for both. Queue it as a proper triggered
        // ability so it uses the stack (and, when cycling, resolves above the cycling draw).
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (scry {})", gameData.id, sourceCard.getName(), trigger.count());
        return true;
    }

    @CollectsTrigger(value = BoostSelfEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleSelfBoostOnDiscard(TriggerMatchContext match, BoostSelfEffect trigger, TriggerContext ctx) {
        // "Whenever you cycle or discard a card, this creature gets +X/+Y until end of turn." Cycling
        // discards the card (CR 702.29e), so this single controller-discard trigger fires for both. Queue
        // it as a proper triggered ability carrying the source permanent id so the self-boost lands on it.
        // (Hekma Sentinels)
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (self-boost)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = SequenceEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    @CollectsTrigger(value = SequenceEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleSequenceOnDiscard(TriggerMatchContext match, SequenceEffect trigger, TriggerContext ctx) {
        // Multi-step discard triggers must stay ONE atomic triggered ability (SequenceEffect), so queue
        // a single stack entry carrying the source permanent id. Each self step then resolves against
        // this creature.
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (sequence)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = SequenceEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARD_EVENT)
    private boolean handleSequenceOnDiscardEvent(TriggerMatchContext match, SequenceEffect trigger,
                                                  TriggerContext ctx) {
        TriggerContext.DiscardEvent discardEvent = (TriggerContext.DiscardEvent) ctx;
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)),
                match.permanent().getId(), discardEvent.discardedCount()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard event (sequence)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = GrantKeywordEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleGrantKeywordOnDiscard(TriggerMatchContext match, GrantKeywordEffect trigger, TriggerContext ctx) {
        // "Whenever you cycle or discard a card, target creature gains [keyword] until end of turn."
        // Cycling discards the card (CR 702.29e), so this single controller-discard trigger fires for
        // both. (Zenith Seeker)
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        if (trigger.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            // Targeted grant: queue a target choice so the controller picks the creature before the
            // ability goes on the stack (resolves above the cycling draw). Serviced by
            // TriggeredAbilityQueueService.processNextDiscardControllerTriggerTarget.
            gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)), match.permanent().getId()));
        } else {
            // Non-targeting grant (self / your creatures) — straight onto the stack as a triggered ability.
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(trigger)),
                    null,
                    match.permanent().getId()));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (grant keyword)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = BoostTargetCreatureEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleBoostTargetCreatureOnDiscard(TriggerMatchContext match, BoostTargetCreatureEffect trigger, TriggerContext ctx) {
        // "Whenever you cycle or discard a card, target creature an opponent controls gets -X/-Y until
        // end of turn." Cycling discards the card (CR 702.29e), so this single controller-discard
        // trigger fires for both. The effect's targetSpec predicate (a "creature an opponent controls"
        // filter) drives the legal-target list via the DiscardControllerTriggerTarget pipeline, so the
        // controller picks the creature before the ability goes on the stack. (Ominous Sphinx)
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)), match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (boost target creature)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = PutCounterOnEachMatchingPermanentEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handlePutCountersOnDiscard(TriggerMatchContext match,
            PutCounterOnEachMatchingPermanentEffect trigger, TriggerContext ctx) {
        // "Whenever you cycle or discard another card, put a -1/-1 counter on each creature your
        // opponents control." Cycling discards the card (CR 702.29e), so this single controller-discard
        // trigger fires for both. Queue it as a proper triggered ability carrying the source permanent id
        // so the "your opponents" predicate resolves against the ability's controller. (Archfiend of Ifnir)
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (put counters on matching permanents)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSourceEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handlePutCountersOnSourceOnDiscard(TriggerMatchContext match,
            PutCountersOnSourceEffect trigger, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on controller discard (put counters on source)",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS),
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS),
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CYCLES)
    })
    private boolean handleMayPayManaOnDiscard(TriggerMatchContext match, MayPayManaEffect trigger, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        if (trigger.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || trigger.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)), match.permanent().getId()));
        } else {
            gameData.enqueueTrigger(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(trigger)),
                    null,
                    match.permanent().getId()));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard/cycle (may pay {})",
                gameData.id, sourceCard.getName(), trigger.manaCost());
        return true;
    }

    /**
     * "Whenever an opponent discards a creature card, create a 2/2 black Zombie creature token."
     * The card-type gate is applied upstream by {@code TriggeringCardConditionalEffect}; here the
     * trigger simply goes on the stack under the watching permanent's controller. (Waste Not)
     */
    @CollectsTrigger(value = CreateTokenEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleCreateTokenOnDiscard(TriggerMatchContext match, CreateTokenEffect trigger, TriggerContext ctx) {
        return enqueueDiscardTrigger(match, trigger, "create token");
    }

    @CollectsTrigger(value = CreateTokenEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleCreateTokenOnControllerDiscard(TriggerMatchContext match,
            CreateTokenEffect trigger, TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on controller discard (create token)",
                gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = CreateTokenEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARD_EVENT)
    private boolean handleCreateTokenOnDiscardEvent(TriggerMatchContext match, CreateTokenEffect trigger,
                                                    TriggerContext ctx) {
        TriggerContext.DiscardEvent discardEvent = (TriggerContext.DiscardEvent) ctx;
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId());
        entry.setEventValue(discardEvent.discardedCount());
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard event (create {} token(s))", gameData.id,
                sourceCard.getName(), discardEvent.discardedCount());
        return true;
    }

    @CollectsTrigger(value = ExileTopCardsMayPlayUntilNextEndStepEffect.class,
            slot = EffectSlot.ON_CONTROLLER_DISCARD_EVENT)
    private boolean handleExileTopCardsMayPlayUntilNextEndStepOnDiscardEvent(
            TriggerMatchContext match, ExileTopCardsMayPlayUntilNextEndStepEffect trigger,
            TriggerContext ctx) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()));
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard event (exile top card)",
                gameData.id, sourceCard.getName());
        return true;
    }

    /**
     * "Whenever an opponent discards a land card, add {B}{B}." Not a mana ability — it does not
     * trigger off a mana ability, so it uses the stack (CR 605.1b) and the mana lands in the
     * controller's pool when it resolves. (Waste Not)
     */
    @CollectsTrigger(value = AwardManaEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    @CollectsTrigger(value = AwardManaEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleAwardManaOnDiscard(TriggerMatchContext match, AwardManaEffect trigger, TriggerContext ctx) {
        return enqueueDiscardTrigger(match, trigger, "add mana");
    }

    /**
     * "Whenever an opponent discards a noncreature, nonland card, draw a card." (Waste Not)
     */
    @CollectsTrigger(value = DrawCardEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleDrawOnDiscard(TriggerMatchContext match, DrawCardEffect trigger, TriggerContext ctx) {
        return enqueueDiscardTrigger(match, trigger, "draw");
    }

    private boolean enqueueDiscardTrigger(TriggerMatchContext match, CardEffect trigger, String what) {
        return enqueueDiscardTrigger(match, trigger, what, null);
    }

    private boolean enqueueDiscardTrigger(TriggerMatchContext match, CardEffect trigger, String what,
            UUID triggeringPlayerId) {
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                triggeringPlayerId,
                match.permanent().getId());
        if (triggeringPlayerId != null) {
            entry.setNonTargeting(true);
        }
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard ({})", gameData.id, sourceCard.getName(), what);
        return true;
    }

    @CollectsTrigger(value = LoseLifeEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    @CollectsTrigger(value = LoseLifeEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleLifeLossOnDiscard(TriggerMatchContext match,
            LoseLifeEffect trigger, TriggerContext ctx) {
        TriggerContext.Discard dc = (TriggerContext.Discard) ctx;
        Card sourceCard = match.permanent().getCard();
        String cardName = sourceCard.getName();
        // Discard-triggered life loss carries a literal amount ("that player loses N life" or
        // "you lose N life").
        int amount = trigger.amount() instanceof Fixed fixed ? fixed.value() : 0;
        var gameData = match.gameData();
        var discardingPlayerId = dc.discardingPlayerId();

        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " triggers — " + gameData.playerIdToName.get(discardingPlayerId) + " loses " + amount + " life."));
        log.info("Game {} - {} triggers on discard, {} loses {} life",
                gameData.id, cardName, gameData.playerIdToName.get(discardingPlayerId), amount);

        if (!gameQueryService.canPlayerLifeChange(gameData, discardingPlayerId)) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(discardingPlayerId) + "'s life total can't change."));
        } else {
            int lifeLoss = amount
                    * gameQueryService.opponentLifeLossMultiplier(gameData, discardingPlayerId);
            int currentLife = gameData.getLife(discardingPlayerId);
            gameData.playerLifeTotals.put(discardingPlayerId, currentLife - lifeLoss);
        }

        return true;
    }
}
