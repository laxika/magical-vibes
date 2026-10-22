package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.VoidEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Void's chosen-number destruction and targeted hand discard. */
@Component
@RequiredArgsConstructor
public class VoidEffectHandler implements NormalEffectHandlerBean {

    private static final int SUGGESTED_MAXIMUM_NUMBER = 16;

    private final PlayerInputService playerInputService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final CardRevealService cardRevealService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return VoidEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        if (gameData.chosenSpellNumber == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginUnboundedSpellNumberChoice(gameData, entry.getControllerId(),
                    SUGGESTED_MAXIMUM_NUMBER);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        int chosenNumber = gameData.chosenSpellNumber;
        gameData.chosenSpellNumber = null;

        destroyMatchingPermanents(gameData, entry, chosenNumber);
        discardMatchingCards(gameData, entry, targetPlayerId, chosenNumber);
    }

    private void destroyMatchingPermanents(GameData gameData, StackEntry entry, int chosenNumber) {
        CardEffect destroyEffect = new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                new PermanentManaValueEqualsXPredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate())))));
        StackEntry destroyEntry = new StackEntry(entry.getEntryType(), entry.getCard(), entry.getControllerId(),
                entry.getDescription(), List.of(), chosenNumber);
        EffectHandler handler = effectHandlerRegistry.getHandler(destroyEffect);
        if (handler == null) {
            throw new IllegalStateException("No handler for " + destroyEffect.getClass().getSimpleName());
        }
        handler.resolve(gameData, destroyEntry, destroyEffect);
    }

    private void discardMatchingCards(GameData gameData, StackEntry entry, UUID targetPlayerId,
                                      int chosenNumber) {
        cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Card> matchingCards = hand.stream()
                .filter(card -> !card.hasType(CardType.LAND) && card.getManaValue() == chosenNumber)
                .toList();
        boolean discardCausedByOpponent = !targetPlayerId.equals(entry.getControllerId());
        gameData.discardCausedByOpponent = discardCausedByOpponent;
        triggerCollectionService.beginDiscardEvent(gameData, targetPlayerId);
        for (Card card : matchingCards) {
            if (!hand.remove(card)) {
                continue;
            }

            if (discardCausedByOpponent && hasEnterBattlefieldOnDiscardEffect(card)) {
                Permanent permanent = new Permanent(card);
                battlefieldEntryService.putPermanentOntoBattlefieldFromOpponentDiscard(
                        gameData, targetPlayerId, permanent);
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(targetPlayerId) + " discards ", card,
                        " — it enters the battlefield instead."));
                if (card.hasType(CardType.CREATURE)) {
                    battlefieldEntryService.handleCreatureEnteredBattlefield(
                            gameData, targetPlayerId, card, null, false);
                }
            } else {
                graveyardService.discardCard(gameData, targetPlayerId, card);
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(targetPlayerId) + " discards ", card, "."));
            }
            triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, card);
        }
        triggerCollectionService.finishDiscardEvent(gameData);
    }

    private boolean hasEnterBattlefieldOnDiscardEffect(Card card) {
        return card.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT).stream()
                .anyMatch(EnterBattlefieldOnDiscardEffect.class::isInstance);
    }
}
