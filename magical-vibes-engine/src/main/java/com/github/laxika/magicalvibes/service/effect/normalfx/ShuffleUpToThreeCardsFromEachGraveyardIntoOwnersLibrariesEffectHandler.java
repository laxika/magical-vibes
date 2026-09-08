package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleUpToThreeCardsFromEachGraveyardIntoOwnersLibrariesEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShuffleUpToThreeCardsFromEachGraveyardIntoOwnersLibrariesEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleUpToThreeCardsFromEachGraveyardIntoOwnersLibrariesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> cards = new ArrayList<>();
        int requiredCount = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null || graveyard.isEmpty()) {
                continue;
            }
            cards.addAll(graveyard);
            requiredCount += Math.min(3, graveyard.size());
        }

        if (cards.isEmpty()) {
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeShuffleUpToThreeCardsFromEachGraveyardResume = true;
        playerInputService.beginMultiGraveyardChoice(
                gameData,
                entry.getControllerId(),
                cards,
                gameData.orderedPlayerIds.size() * 3,
                requiredCount,
                "Choose three cards in each graveyard to shuffle into their owners' libraries.");
    }
}
