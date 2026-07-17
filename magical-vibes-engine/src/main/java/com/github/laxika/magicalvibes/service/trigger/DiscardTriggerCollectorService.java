package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.CardColor;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDiscardingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
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
        String triggerLog = match.permanent().getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.text(triggerLog));
        log.info("Game {} - {} triggers on discard (may ability)", match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = DealDamageToDiscardingPlayerEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleDamageOnDiscard(TriggerMatchContext match,
            DealDamageToDiscardingPlayerEffect trigger, TriggerContext ctx) {
        TriggerContext.Discard dc = (TriggerContext.Discard) ctx;
        String cardName = match.permanent().getCard().getName();
        int damage = trigger.damage();
        var gameData = match.gameData();
        var discardingPlayerId = dc.discardingPlayerId();

        String logEntry = cardName + " triggers — deals " + damage + " damage to "
                + gameData.playerIdToName.get(discardingPlayerId) + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} triggers on discard, dealing {} damage to {}",
                gameData.id, cardName, damage, gameData.playerIdToName.get(discardingPlayerId));

        CardColor sourceColor = gameQueryService.getEffectiveColor(gameData, match.permanent());
        if (!gameQueryService.isDamageFromSourcePrevented(gameData, sourceColor)
                && !damagePreventionService.isSourceDamagePreventedForPlayer(gameData, discardingPlayerId, match.permanent().getId())
                && !gameData.permanentsPreventedFromDealingDamage.contains(match.permanent().getId())
                && !damagePreventionService.applyColorDamagePreventionForPlayer(gameData, discardingPlayerId, sourceColor)) {
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, discardingPlayerId, damage);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, discardingPlayerId, effectiveDamage, cardName);
            if (effectiveDamage > 0 && gameQueryService.shouldDamageBeDealtAsInfect(gameData, discardingPlayerId)) {
                if (gameQueryService.canPlayerGetPoisonCounters(gameData, discardingPlayerId)) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(discardingPlayerId, 0);
                    gameData.playerPoisonCounters.put(discardingPlayerId, currentPoison + effectiveDamage);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(gameData.playerIdToName.get(discardingPlayerId) + " gets " + effectiveDamage + " poison counters from " + cardName + "."));
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

        String cardName = match.permanent().getCard().getName();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(cardName + " exiles "
                + discarded.getName() + " from " + gameData.playerIdToName.get(ownerId) + "'s graveyard."));
        log.info("Game {} - {} exiles discarded card {} from graveyard", gameData.id, cardName, discarded.getName());
        return true;
    }

    @CollectsTrigger(value = LoseLifeEffect.class, slot = EffectSlot.ON_OPPONENT_DISCARDS)
    private boolean handleLifeLossOnDiscard(TriggerMatchContext match,
            LoseLifeEffect trigger, TriggerContext ctx) {
        TriggerContext.Discard dc = (TriggerContext.Discard) ctx;
        String cardName = match.permanent().getCard().getName();
        // The ON_OPPONENT_DISCARDS marker always carries a literal amount ("that player loses N life").
        int amount = trigger.amount() instanceof Fixed fixed ? fixed.value() : 0;
        var gameData = match.gameData();
        var discardingPlayerId = dc.discardingPlayerId();

        String logEntry = cardName + " triggers — " + gameData.playerIdToName.get(discardingPlayerId)
                + " loses " + amount + " life.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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
