package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandAndDiscardDuplicateNonlandCardsEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RevealHandAndDiscardDuplicateNonlandCardsEffect} from the target player's
 * revealed hand. Duplicate names are determined before any card is discarded, so every matching
 * nonland card is discarded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealHandAndDiscardDuplicateNonlandCardsEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealHandAndDiscardDuplicateNonlandCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        gameData.discardCausedByOpponent = !targetPlayerId.equals(entry.getControllerId());
        if (gameData.discardCausedByOpponent && gameQueryService.isDiscardPrevented(gameData, targetPlayerId)) {
            return;
        }

        Set<String> duplicateNames = new HashSet<>();
        Set<String> seenNames = new HashSet<>();
        for (Card card : hand) {
            if (!seenNames.add(card.getName())) {
                duplicateNames.add(card.getName());
            }
        }

        List<Card> toDiscard = new ArrayList<>();
        for (Card card : hand) {
            if (!card.hasType(CardType.LAND) && duplicateNames.contains(card.getName())) {
                toDiscard.add(card);
            }
        }
        triggerCollectionService.beginDiscardEvent(gameData, targetPlayerId);
        for (Card card : toDiscard) {
            discardCard(gameData, targetPlayerId, card, entry.getCard());
        }
        triggerCollectionService.finishDiscardEvent(gameData);
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    }

    private void discardCard(GameData gameData, UUID playerId, Card card, Card sourceCard) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || !hand.remove(card)) {
            return;
        }
        boolean entersBattlefield = gameData.discardCausedByOpponent
                && card.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT).stream()
                .anyMatch(EnterBattlefieldOnDiscardEffect.class::isInstance);
        if (entersBattlefield) {
            battlefieldEntryService.putPermanentOntoBattlefieldFromOpponentDiscard(
                    gameData, playerId, new Permanent(card));
        } else {
            graveyardService.discardCard(gameData, playerId, card);
        }
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " discards ", card, "."));
        log.info("Game {} - {} discards {} ({})", gameData.id,
                gameData.playerIdToName.get(playerId), card.getName(), sourceCard.getName());
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        if (entersBattlefield && card.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, playerId, card, null, false);
        }
    }
}
