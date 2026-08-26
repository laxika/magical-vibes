package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a death trigger that puts the triggering card on the bottom of its owner's library. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTriggeringCardFromGraveyardOnBottomOfLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID dyingCardId = ((PutTriggeringCardFromGraveyardOnBottomOfLibraryEffect) effect).dyingCardId();
        if (dyingCardId == null) {
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        Card deadCard = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        if (ownerId == null || deadCard == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} death trigger fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCardId);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);
        List<Card> library = gameData.playerDecks.get(ownerId);
        library.add(deadCard);
        gameData.playerDecks.get(ownerId).add(deadCard);

        String ownerName = gameData.playerIdToName.get(ownerId);
        gameLogService.append(gameData, GameLog.builder()
                .card(deadCard)
                .text(" is put on the bottom of " + ownerName + "'s library.")
                .build());
        log.info("Game {} - {} puts {} on the bottom of {}'s library",
                gameData.id, entry.getCard().getName(), deadCard.getName(), ownerName);
    }
}
