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
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDiscardingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.github.laxika.magicalvibes.model.GameLog;
/**
 * Trigger collectors for discard events (ON_OPPONENT_DISCARDS).
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

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleDiscardMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers on discard (may ability)", match.gameData().id, match.permanent().getCard().getName());
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

        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " triggers — deals " + damage + " damage to " + gameData.playerIdToName.get(discardingPlayerId) + "."));
        log.info("Game {} - {} triggers on discard, dealing {} damage to {}",
                gameData.id, cardName, damage, gameData.playerIdToName.get(discardingPlayerId));

        CardColor sourceColor = gameQueryService.getEffectiveColor(gameData, match.permanent());
        boolean sourceDamagePrevented = damagePreventionService.isSourceDamagePreventedForPlayer(
                gameData, discardingPlayerId, match.permanent().getId());
        if (sourceDamagePrevented && !gameQueryService.isDamageFromSourcePrevented(gameData, sourceColor)) {
            damagePreventionService.applySourceDamagePreventionForPlayer(
                    gameData, discardingPlayerId, match.permanent().getId(), damage,
                    gameQueryService.getEffectiveColors(gameData, match.permanent()));
        }
        if (!gameQueryService.isDamageFromSourcePrevented(gameData, sourceColor)
                && !sourceDamagePrevented
                && !gameData.isPreventedFromDealingDamage(match.permanent().getId())
                && !damagePreventionService.applyColorDamagePreventionForPlayer(gameData, discardingPlayerId, sourceColor)) {
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, discardingPlayerId, damage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, discardingPlayerId, effectiveDamage, cardName);
            if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, discardingPlayerId)) {
                if (gameQueryService.canPlayerGetPoisonCounters(gameData, discardingPlayerId)) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(discardingPlayerId, 0);
                    gameData.playerPoisonCounters.put(discardingPlayerId, currentPoison + effectiveDamage);
                    gameLogService.append(gameData, GameLog.textCardText(
                            gameData.playerIdToName.get(discardingPlayerId) + " gets " + effectiveDamage + " poison counters from ",
                            sourceCard, "."));
                }
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, discardingPlayerId)) {
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(discardingPlayerId) + "'s life total can't change."));
            } else {
                int currentLife = gameData.getLife(discardingPlayerId);
                gameData.playerLifeTotals.put(discardingPlayerId, currentLife - effectiveDamage);
            }
            if (effectiveDamage > 0) {
                gameData.recordDamageToPlayer(discardingPlayerId, effectiveDamage);
                triggerCollectionService.checkOpponentDealtDamageTriggers(gameData, discardingPlayerId, effectiveDamage);
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
    private boolean handleSequenceOnDiscard(TriggerMatchContext match, SequenceEffect trigger, TriggerContext ctx) {
        // "Whenever you cycle or discard a card, this creature gets +X/+Y until end of turn and can't be
        // blocked this turn" (and similar mandatory multi-step self-triggers). Cycling discards the card
        // (CR 702.29e), so this single controller-discard trigger fires for both. The steps must stay ONE
        // atomic triggered ability (SequenceEffect), so queue a single stack entry carrying the source
        // permanent id — each self step then resolves against this creature. (Cunning Survivor)
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

    @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleMayPayManaOnDiscard(TriggerMatchContext match, MayPayManaEffect trigger, TriggerContext ctx) {
        // "Whenever you cycle or discard a card, you may pay {N}. If you do, ..." Cycling discards the card
        // (CR 702.29e), so this single controller-discard trigger fires for both. Queue it as a proper
        // triggered ability so it uses the stack (and, when cycling, resolves above the cycling draw); its
        // MayAbilityChoice pay prompt then comes up at resolution. (Drake Haven)
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
        log.info("Game {} - {} triggers on cycle/discard (may pay {})", gameData.id, sourceCard.getName(), trigger.manaCost());
        return true;
    }

    /**
     * "Whenever an opponent discards a creature card, create a 2/2 black Zombie creature token."
     * The card-type gate is applied upstream by {@code TriggeringCardConditionalEffect}; here the
     * trigger simply goes on the stack under the watching permanent's controller. (Waste Not)
     */
    @CollectsTrigger(value = CreateTokenEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleCreateTokenOnDiscard(TriggerMatchContext match, CreateTokenEffect trigger, TriggerContext ctx) {
        return enqueueOpponentDiscardTrigger(match, trigger, "create token");
    }

    /**
     * "Whenever an opponent discards a land card, add {B}{B}." Not a mana ability — it does not
     * trigger off a mana ability, so it uses the stack (CR 605.1b) and the mana lands in the
     * controller's pool when it resolves. (Waste Not)
     */
    @CollectsTrigger(value = AwardManaEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleAwardManaOnDiscard(TriggerMatchContext match, AwardManaEffect trigger, TriggerContext ctx) {
        return enqueueOpponentDiscardTrigger(match, trigger, "add mana");
    }

    /**
     * "Whenever an opponent discards a noncreature, nonland card, draw a card." (Waste Not)
     */
    @CollectsTrigger(value = DrawCardEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleDrawOnDiscard(TriggerMatchContext match, DrawCardEffect trigger, TriggerContext ctx) {
        return enqueueOpponentDiscardTrigger(match, trigger, "draw");
    }

    private boolean enqueueOpponentDiscardTrigger(TriggerMatchContext match, CardEffect trigger, String what) {
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
        log.info("Game {} - {} triggers on opponent discard ({})", gameData.id, sourceCard.getName(), what);
        return true;
    }

    @CollectsTrigger(value = LoseLifeEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleLifeLossOnDiscard(TriggerMatchContext match,
            LoseLifeEffect trigger, TriggerContext ctx) {
        TriggerContext.Discard dc = (TriggerContext.Discard) ctx;
        Card sourceCard = match.permanent().getCard();
        String cardName = sourceCard.getName();
        // The ON_OPPONENT_DISCARDS marker always carries a literal amount ("that player loses N life").
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
            int currentLife = gameData.getLife(discardingPlayerId);
            gameData.playerLifeTotals.put(discardingPlayerId, currentLife - amount);
        }

        return true;
    }
}
