package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileHandThenDrawAndMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a hand exile followed by an equal draw and temporary play permission. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileHandThenDrawAndMayPlayUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final ExileSupport exileSupport;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileHandThenDrawAndMayPlayUntilNextTurnEffect.class;
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
            exileService.exileCard(gameData, controllerId, card);
            exileSupport.grantPlayUntilOwnersNextTurn(gameData, card.getId(), controllerId);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();
        int count = cardsToExile.size();
        gameLogService.append(gameData, GameLog.text(playerName + " exiles " + count
                + " card" + (count != 1 ? "s" : "")
                + " from their hand and may play them until the end of their next turn."));

        for (int i = 0; i < count; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }
        gameLogService.append(gameData, GameLog.text(playerName + " draws " + count
                + " card" + (count != 1 ? "s." : ".")));
        log.info("Game {} - {} exiles {} hand cards and may play them until their next turn for {}",
                gameData.id, playerName, count, cardName);
    }
}
