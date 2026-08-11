package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesTopUntilTotalManaValueMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Dream Harvest: each opponent digs until their exiled cards reach a total mana value threshold;
 * the controller may cast the exiled spells for free this turn.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachOpponentExilesTopUntilTotalManaValueMayCastThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentExilesTopUntilTotalManaValueMayCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentExilesTopUntilTotalManaValueMayCastThisTurnEffect typedEffect =
                (EachOpponentExilesTopUntilTotalManaValueMayCastThisTurnEffect) effect;
        UUID controllerId = entry.getControllerId();
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (!opponentId.equals(controllerId)) {
                exileUntilThreshold(gameData, entry, typedEffect.totalManaValueThreshold(),
                        controllerId, opponentId);
            }
        }
    }

    private void exileUntilThreshold(GameData gameData, StackEntry entry, int threshold,
                                     UUID controllerId, UUID opponentId) {
        List<Card> deck = gameData.playerDecks.get(opponentId);
        String opponentName = gameData.playerIdToName.get(opponentId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (deck == null || deck.isEmpty() || threshold <= 0) {
            return;
        }

        List<Card> exiled = new ArrayList<>();
        int totalManaValue = 0;
        while (totalManaValue < threshold && !deck.isEmpty()) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, opponentId, topCard);
            exiled.add(topCard);
            totalManaValue += topCard.getManaValue();

            if (!topCard.hasType(CardType.LAND)) {
                gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
                gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());
                gameData.exilePlayWithoutPayingManaCost.add(topCard.getId());
            }
        }

        StringBuilder logText = new StringBuilder(opponentName)
                .append(" exiles ").append(exiled.size())
                .append(" card(s) from the top of their library (total mana value ")
                .append(totalManaValue).append(").");
        if (exiled.stream().anyMatch(card -> !card.hasType(CardType.LAND))) {
            logText.append(" ").append(controllerName)
                    .append(" may cast the exiled nonland cards without paying their mana costs this turn.");
        }
        gameLogService.append(gameData, GameLog.text(logText.toString()));
        log.info("Game {} - {} exiles {} cards from library top for {}, total mana value {}",
                gameData.id, opponentName, exiled.size(), entry.getCard().getName(), totalManaValue);
    }
}
