package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryAndReturnExiledCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutSelfOnBottomOfOwnersLibraryAndReturnExiledCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutSelfOnBottomOfOwnersLibraryAndReturnExiledCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, entry.getCard().getId());
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, entry.getCard().getId());
        if (sourceCard == null || ownerId == null || gameData.playerDecks.get(ownerId) == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, sourceCard.getId());
        if (gameQueryService.findCardInGraveyardById(gameData, sourceCard.getId()) != null) {
            return;
        }
        gameData.playerDecks.get(ownerId).add(sourceCard);
        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " is put on the bottom of its owner's library."));

        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        if (sourcePermanentId == null) {
            return;
        }

        UUID trackedSourcePermanentId = sourcePermanentId;
        List<ExiledCardEntry> toReturn = gameData.exiledCards.stream()
                .filter(exiled -> trackedSourcePermanentId.equals(exiled.sourcePermanentId()))
                .toList();
        for (ExiledCardEntry exiled : toReturn) {
            if (!gameData.removeFromExile(exiled.card().getId())) {
                continue;
            }
            gameData.addCardToHand(exiled.ownerId(), exiled.card());
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(exiled.ownerId()) + " puts ",
                    exiled.card(), " from exile into their hand."));
            log.info("Game {} - {} returns {} from exile to its owner's hand via {}",
                    gameData.id, gameData.playerIdToName.get(exiled.ownerId()),
                    exiled.card().getName(), entry.getCard().getName());
        }
    }
}
