package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromAnyGraveyardThenMayBecomeCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Starts Lazav, Familiar Stranger's non-targeting graveyard choice. The selected card is exiled
 * and the conditional copy choice is completed by {@code GraveyardChoiceHandlerService}.
 */
@Component
@RequiredArgsConstructor
public class ExileCardFromAnyGraveyardThenMayBecomeCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardFromAnyGraveyardThenMayBecomeCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> graveyardCards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard != null) {
                graveyardCards.addAll(graveyard);
            }
        }

        if (graveyardCards.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no cards in any graveyard to exile."));
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeExileThenMayBecomeCopyResume = true;
        playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(), graveyardCards, 1,
                "You may exile a card from a graveyard.");
    }
}
