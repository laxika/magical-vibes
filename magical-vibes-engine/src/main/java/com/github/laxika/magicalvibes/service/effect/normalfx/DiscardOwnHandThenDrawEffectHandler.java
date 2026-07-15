package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardOwnHandThenDrawEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardOwnHandThenDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardOwnHandThenDrawEffect) effect;
        UUID controllerId = entry.getControllerId();

        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();
        List<Card> hand = gameData.playerHands.get(controllerId);

        if (hand != null && !hand.isEmpty()) {
            List<Card> discarded = new ArrayList<>(hand);
            int discardCount = discarded.size();
            hand.clear();
            gameData.discardCausedByOpponent = false;

            for (Card discardedCard : discarded) {
                graveyardService.addCardToGraveyard(gameData, controllerId, discardedCard);
                triggerCollectionService.checkDiscardTriggers(gameData, controllerId, discardedCard);
            }

            String discardLog = playerName + " discards their hand (" + discardCount
                    + " card" + (discardCount != 1 ? "s" : "") + ") (" + cardName + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(discardLog));
            log.info("Game {} - {} discards hand of {} cards for {}", gameData.id, playerName, discardCount, cardName);
        }

        int drawCount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, null));
        for (int i = 0; i < drawCount; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }
        if (drawCount > 0) {
            String drawLog = playerName + " draws " + drawCount + " card" + (drawCount != 1 ? "s" : "") + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(drawLog));
            log.info("Game {} - {} draws {} cards for {}", gameData.id, playerName, drawCount, cardName);
        }
    }
}
