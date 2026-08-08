package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndDiscardSplicedNamesEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CounterSpellAndDiscardSplicedNamesEffect}: counters the target spell, then its
 * controller reveals their hand and discards every card sharing a name with a card spliced onto that
 * spell. The spliced names were recorded at cast time in {@link GameData#spellCastSplicedNames}; with
 * none the effect is a plain counterspell and no hand is revealed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CounterSpellAndDiscardSplicedNamesEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellAndDiscardSplicedNamesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) {
            return;
        }
        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) {
            return;
        }

        UUID controllerId = targetEntry.getControllerId();
        List<String> splicedNames = gameData.getSpellCastSplicedNames(targetEntry.getCard().getId());
        counterSupport.counterSpell(gameData, entry, targetEntry);
        gameData.clearSpellCastSplicedNames(targetEntry.getCard().getId());

        if (splicedNames.isEmpty() || controllerId == null) {
            return;
        }

        cardRevealService.revealHandToAllPlayers(gameData, controllerId);

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }
        List<Card> toDiscard = new ArrayList<>();
        for (Card card : hand) {
            if (splicedNames.contains(card.getName())) {
                toDiscard.add(card);
            }
        }
        for (Card card : toDiscard) {
            discardCard(gameData, controllerId, card, entry.getCard());
        }
    }

    private void discardCard(GameData gameData, UUID playerId, Card card, Card sourceCard) {
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || !hand.remove(card)) {
            return;
        }
        gameData.discardCausedByOpponent = true;
        graveyardService.discardCard(gameData, playerId, card);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " discards ", card, "."));
        log.info("Game {} - {} discards {} ({})", gameData.id,
                gameData.playerIdToName.get(playerId), card.getName(), sourceCard.getName());
        triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    }
}
