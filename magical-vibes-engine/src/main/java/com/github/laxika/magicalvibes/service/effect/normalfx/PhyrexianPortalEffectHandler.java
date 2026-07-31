package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhyrexianPortalEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PhyrexianPortalEffect}: the ability does nothing unless the controller's library
 * has ten or more cards. Otherwise the top ten cards are handed to the target opponent to split
 * into two face-down piles, using the shared card-pile separation flow with
 * {@link CardPileDisposition#SEARCH_ONE_TO_HAND}; {@code GraveyardReturnSupport} completes both
 * steps (exile the unchosen pile, search the chosen one).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PhyrexianPortalEffectHandler implements NormalEffectHandlerBean {

    private static final int PILE_SIZE = 10;

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhyrexianPortalEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (deck == null || deck.size() < PILE_SIZE) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library has fewer than ten cards; nothing happens."));
            return;
        }
        if (opponentId == null) {
            return;
        }

        List<Card> pileCards = new ArrayList<>();
        Map<UUID, UUID> cardOwners = new HashMap<>();
        for (int i = 0; i < PILE_SIZE; i++) {
            Card card = deck.removeFirst();
            pileCards.add(card);
            cardOwners.put(card.getId(), controllerId);
        }

        gameData.queueInteraction(new PendingPileSeparation(controllerId, opponentId,
                List.of(), pileCards, cardOwners, List.of(), List.of(),
                CardPileDisposition.SEARCH_ONE_TO_HAND));
        playerInputService.beginMultiGraveyardChoice(gameData, opponentId, pileCards, pileCards.size(),
                "Look at the top ten cards of " + playerName + "'s library and separate them into two "
                        + "face-down piles. Select cards for Pile 1 (unselected cards form Pile 2).");
    }
}
