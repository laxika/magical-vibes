package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardAdvantageSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves post-spell card-advantage trigger markers at the point the spell finishes resolving. */
public final class CardAdvantagePostResolutionService {

    private CardAdvantagePostResolutionService() {
    }

    /**
     * Recaptures the card counts a card-advantage trigger compares against, as the spell begins to
     * resolve: card advantage is measured against the state before that spell resolved, so changes
     * made by spells that resolved after it was cast do not count toward it.
     */
    public static void snapshotBeforeResolution(GameData gameData, StackEntry spellEntry) {
        List<CardEffect> pending = spellEntry.getPostResolutionEffects();
        if (pending.isEmpty()) {
            return;
        }
        List<CardEffect> updated = new java.util.ArrayList<>(pending.size());
        for (CardEffect effect : pending) {
            if (effect instanceof CardAdvantageSpellCastTriggerEffect trigger && trigger.isPrepared()) {
                Map<UUID, Integer> counts = new HashMap<>();
                for (UUID playerId : gameData.playerIds) {
                    counts.put(playerId, cardResourceCount(gameData, playerId));
                }
                if (trigger.handSpellOwnerId() != null) {
                    counts.merge(trigger.handSpellOwnerId(), 1, Integer::sum);
                }
                updated.add(trigger.withCardCountsBefore(counts));
            } else {
                updated.add(effect);
            }
        }
        spellEntry.replacePostResolutionEffects(updated);
    }

    public static void process(GameData gameData, StackEntry spellEntry) {
        for (CardEffect effect : spellEntry.takePostResolutionEffects()) {
            if (!(effect instanceof CardAdvantageSpellCastTriggerEffect trigger)
                    || !trigger.isPrepared()
                    || !causedCardAdvantage(gameData, trigger)
                    || !sourceOnBattlefield(gameData, trigger.sourcePermanentId())) {
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

    private static boolean sourceOnBattlefield(GameData gameData, UUID sourcePermanentId) {
        return gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(List::stream)
                .anyMatch(permanent -> permanent.getId().equals(sourcePermanentId));
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
