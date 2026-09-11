package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesTopUntilTotalManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachOpponentExilesTopUntilTotalManaValueEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentExilesTopUntilTotalManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentExilesTopUntilTotalManaValueEffect typedEffect =
                (EachOpponentExilesTopUntilTotalManaValueEffect) effect;
        UUID controllerId = entry.getControllerId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                exileUntilThreshold(gameData, entry, typedEffect.totalManaValueThreshold(), playerId);
            }
        }
    }

    private void exileUntilThreshold(GameData gameData, StackEntry entry, int threshold,
                                     UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null || deck.isEmpty() || threshold <= 0) {
            return;
        }

        List<Card> exiled = new ArrayList<>();
        int totalManaValue = 0;
        while (totalManaValue < threshold && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, playerId, topCard);
            exiled.add(topCard);
            totalManaValue += topCard.getManaValue();
        }

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.text(playerName + " exiles " + exiled.size()
                + " card(s) from the top of their library (total mana value " + totalManaValue
                + ")."));
        log.info("Game {} - {} exiles {} cards from library top for {}, total mana value {}",
                gameData.id, playerName, exiled.size(), entry.getCard().getName(), totalManaValue);
    }
}
