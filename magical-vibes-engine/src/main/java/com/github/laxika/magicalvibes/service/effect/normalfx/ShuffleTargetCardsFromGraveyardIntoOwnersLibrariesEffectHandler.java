package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetCardIds = entry.getTargetCardIdsForEffect(effect);
        if (targetCardIds == null || targetCardIds.isEmpty()) {
            return;
        }

        Map<UUID, List<Card>> movedCardsByOwner = new LinkedHashMap<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID cardId : targetCardIds) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                if (card == null || ownerId == null) {
                    continue;
                }

                List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
                if (graveyard != null && graveyard.removeIf(candidate -> candidate.getId().equals(cardId))) {
                    gameData.playerDecks.get(ownerId).add(card);
                    movedCardsByOwner.computeIfAbsent(ownerId, ignored -> new ArrayList<>()).add(card);
                    graveyardService.notifyCardsLeftGraveyard(gameData, ownerId, card);
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        for (Map.Entry<UUID, List<Card>> movedEntry : movedCardsByOwner.entrySet()) {
            UUID ownerId = movedEntry.getKey();
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(ownerId) + " shuffles cards into their library (",
                    entry.getCard(), ")."));
        }
    }
}
