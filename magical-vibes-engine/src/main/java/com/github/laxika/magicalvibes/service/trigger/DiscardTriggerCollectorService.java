package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.CardColor;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDiscardingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final DamagePreventionService damagePreventionService;
    private final PermanentRemovalService permanentRemovalService;

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleDiscardMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(sourceCard,
                " triggers — deals " + damage + " damage to " + gameData.playerIdToName.get(discardingPlayerId) + "."));
        log.info("Game {} - {} triggers on discard, dealing {} damage to {}",
                gameData.id, cardName, damage, gameData.playerIdToName.get(discardingPlayerId));

        CardColor sourceColor = gameQueryService.getEffectiveColor(gameData, match.permanent());
        if (!gameQueryService.isDamageFromSourcePrevented(gameData, sourceColor)
                && !damagePreventionService.isSourceDamagePreventedForPlayer(gameData, discardingPlayerId, match.permanent().getId())
                && !gameData.isPreventedFromDealingDamage(match.permanent().getId())
                && !damagePreventionService.applyColorDamagePreventionForPlayer(gameData, discardingPlayerId, sourceColor)) {
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, discardingPlayerId, damage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, discardingPlayerId, effectiveDamage, cardName);
            if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, discardingPlayerId)) {
                if (gameQueryService.canPlayerGetPoisonCounters(gameData, discardingPlayerId)) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(discardingPlayerId, 0);
                    gameData.playerPoisonCounters.put(discardingPlayerId, currentPoison + effectiveDamage);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                            gameData.playerIdToName.get(discardingPlayerId) + " gets " + effectiveDamage + " poison counters from ",
                            sourceCard, "."));
                }
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, discardingPlayerId)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(gameData.playerIdToName.get(discardingPlayerId) + "'s life total can't change."));
            } else {
                int currentLife = gameData.getLife(discardingPlayerId);
                gameData.playerLifeTotals.put(discardingPlayerId, currentLife - effectiveDamage);
            }
            if (effectiveDamage > 0) {
                gameData.recordDamageToPlayer(discardingPlayerId, effectiveDamage);
            }
        }

        return true;
    }

    @CollectsTrigger(value = ExileDiscardedCardFromGraveyardEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
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

        permanentRemovalService.removeCardFromGraveyardById(gameData, discarded.getId());
        gameData.addToExile(ownerId, discarded);

        Card sourceCard = match.permanent().getCard();
        String cardName = sourceCard.getName();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(sourceCard, " exiles ", discarded,
                " from " + gameData.playerIdToName.get(ownerId) + "'s graveyard."));
        log.info("Game {} - {} exiles discarded card {} from graveyard", gameData.id, cardName, discarded.getName());
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(sourceCard));
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (self-boost)", gameData.id, sourceCard.getName());
        return true;
    }

    @CollectsTrigger(value = GrantKeywordEffect.class, slot = EffectSlot.ON_CONTROLLER_DISCARDS)
    private boolean handleGrantKeywordOnDiscard(TriggerMatchContext match, GrantKeywordEffect trigger, TriggerContext ctx) {
        // "Whenever you cycle or discard a card, target creature gains [keyword] until end of turn."
        // Cycling discards the card (CR 702.29e), so this single controller-discard trigger fires for
        // both. (Zenith Seeker)
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        if (trigger.targetSpec().category().includesPermanents()) {
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (grant keyword)", gameData.id, sourceCard.getName());
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(sourceCard));
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on cycle/discard (may pay {})", gameData.id, sourceCard.getName(), trigger.manaCost());
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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(sourceCard,
                " triggers — " + gameData.playerIdToName.get(discardingPlayerId) + " loses " + amount + " life."));
        log.info("Game {} - {} triggers on discard, {} loses {} life",
                gameData.id, cardName, gameData.playerIdToName.get(discardingPlayerId), amount);

        if (!gameQueryService.canPlayerLifeChange(gameData, discardingPlayerId)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(gameData.playerIdToName.get(discardingPlayerId) + "'s life total can't change."));
        } else {
            int currentLife = gameData.getLife(discardingPlayerId);
            gameData.playerLifeTotals.put(discardingPlayerId, currentLife - amount);
        }

        return true;
    }
}
