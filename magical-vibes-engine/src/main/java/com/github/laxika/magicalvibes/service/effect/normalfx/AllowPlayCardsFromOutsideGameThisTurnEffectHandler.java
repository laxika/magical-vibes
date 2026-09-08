package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromOutsideGameThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AllowPlayCardsFromOutsideGameThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowPlayCardsFromOutsideGameThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> sideboard = gameData.playerSideboards.get(controllerId);
        if (sideboard != null) {
            for (Card card : List.copyOf(sideboard)) {
                gameData.outsideGamePlayPermissions.add(card.getId());
            }
        }

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " may play cards from outside the game this turn."));
    }
}
