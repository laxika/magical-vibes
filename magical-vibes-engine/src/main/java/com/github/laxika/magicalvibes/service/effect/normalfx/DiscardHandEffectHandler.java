package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DiscardHandEffect}: each affected player discards their entire hand. Single-player
 * recipients (controller / target player) discard directly; each-player recipients iterate in APNAP
 * order (active player first). There is no player choice — every card goes to the graveyard, firing
 * discard triggers per card.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardHandEffect) effect;
        String cardName = entry.getCard().getName();
        UUID controllerId = entry.getControllerId();

        switch (e.recipient()) {
            case CONTROLLER -> discardHand(gameData, controllerId, controllerId, cardName);
            case TARGET_PLAYER -> discardHand(gameData, entry.getTargetId(), controllerId, cardName);
            case EACH_PLAYER, EACH_OPPONENT -> {
                boolean opponentsOnly = e.recipient() == DiscardRecipient.EACH_OPPONENT;
                for (UUID playerId : apnapOrder(gameData)) {
                    if (opponentsOnly && playerId.equals(controllerId)) {
                        continue;
                    }
                    discardHand(gameData, playerId, controllerId, cardName);
                }
            }
        }
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null) {
            order.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                order.add(playerId);
            }
        }
        return order;
    }

    private void discardHand(GameData gameData, UUID playerId, UUID controllerId, String cardName) {
        String playerName = gameData.playerIdToName.get(playerId);
        List<Card> hand = gameData.playerHands.get(playerId);

        if (hand == null || hand.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " has no cards to discard (" + cardName + ").");
            log.info("Game {} - {} has no cards to discard for {}", gameData.id, playerName, cardName);
            return;
        }

        List<Card> discarded = new ArrayList<>(hand);
        hand.clear();
        gameData.discardCausedByOpponent = !playerId.equals(controllerId);

        for (Card card : discarded) {
            graveyardService.addCardToGraveyard(gameData, playerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        }

        gameBroadcastService.logAndBroadcast(gameData, playerName + " discards their hand ("
                + discarded.size() + " card" + (discarded.size() != 1 ? "s" : "") + ") (" + cardName + ").");
        log.info("Game {} - {} discards hand of {} cards for {}", gameData.id, playerName, discarded.size(), cardName);
    }
}
