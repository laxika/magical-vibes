package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardAdvantageSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;
import java.util.UUID;

/** Resolves post-spell card-advantage trigger markers at the point the spell finishes resolving. */
public final class CardAdvantagePostResolutionService {

    private CardAdvantagePostResolutionService() {
    }

    public static void process(GameData gameData, StackEntry spellEntry) {
        for (CardEffect effect : spellEntry.takePostResolutionEffects()) {
            if (!(effect instanceof CardAdvantageSpellCastTriggerEffect trigger)
                    || !trigger.isPrepared()
                    || !causedCardAdvantage(gameData, trigger)) {
                continue;
            }

            StackEntry triggerEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    trigger.sourceCard(),
                    trigger.controllerId(),
                    trigger.sourceCard().getName() + "'s ability",
                    List.of(new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT)),
                    null,
                    trigger.sourcePermanentId());
            triggerEntry.setTriggeringCardId(trigger.spellCardId());
            triggerEntry.setNonTargeting(true);
            gameData.stack.add(triggerEntry);
        }
    }

    private static boolean causedCardAdvantage(GameData gameData,
                                                CardAdvantageSpellCastTriggerEffect trigger) {
        UUID controllerId = trigger.controllerId();
        int controllerDelta = cardResourceCount(gameData, controllerId)
                - trigger.cardCountsBefore().getOrDefault(controllerId, 0);
        int opponentsDelta = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .mapToInt(playerId -> cardResourceCount(gameData, playerId)
                        - trigger.cardCountsBefore().getOrDefault(playerId, 0))
                .sum();
        return controllerDelta > opponentsDelta;
    }

    private static int cardResourceCount(GameData gameData, UUID playerId) {
        int handCards = (int) gameData.playerHands.getOrDefault(playerId, List.of()).stream()
                .filter(card -> isCard(gameData, card))
                .count();
        int battlefieldCards = (int) gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> isCard(gameData, permanent.getCard()))
                .count();
        return handCards + battlefieldCards;
    }

    private static boolean isCard(GameData gameData, Card card) {
        return card != null && !card.isToken() && !gameData.dynamicTokenCardIds.contains(card.getId());
    }
}
