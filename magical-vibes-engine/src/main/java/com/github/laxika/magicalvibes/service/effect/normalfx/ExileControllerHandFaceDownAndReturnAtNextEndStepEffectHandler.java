package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileControllerHandFaceDownAndReturnAtNextEndStepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Ignorant Bliss's hand exile and registers its delayed returns. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileControllerHandFaceDownAndReturnAtNextEndStepEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileControllerHandFaceDownAndReturnAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Card> cardsToExile = new ArrayList<>(hand);
        hand.clear();
        for (Card card : cardsToExile) {
            exileService.exileCardFaceDown(gameData, controllerId, card, null);
            gameData.queueDelayedAction(new ReturnExiledCardToHandAtNextEndStep(
                    card.getId(), controllerId, entry.getCard()));
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(playerName + " exiles " + cardsToExile.size()
                + " card" + (cardsToExile.size() != 1 ? "s" : "")
                + " from their hand face down."));
        log.info("Game {} - {} exiles {} hand cards face down until the next end step",
                gameData.id, playerName, cardsToExile.size());
    }
}
