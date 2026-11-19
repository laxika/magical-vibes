package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardIntoLibraryEffect.Destination;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves a trigger that puts the triggering card from its owner's graveyard on top or on the bottom of its owner's
 * library. A card that left the graveyard and came back is a new object the trigger no longer refers to (CR 400.7), so
 * the ability does nothing then.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTriggeringCardFromGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTriggeringCardFromGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Destination destination = ((PutTriggeringCardFromGraveyardIntoLibraryEffect) effect).destination();
        UUID dyingCardId = entry.getTriggeringCardId();
        if (dyingCardId == null) {
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        Card deadCard = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        if (ownerId == null || deadCard == null
                || !entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(dyingCardId))) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} trigger fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCardId);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);
        List<Card> library = gameData.playerDecks.get(ownerId);
        String end = destination == Destination.TOP ? "on top of " : "on the bottom of ";
        if (destination == Destination.TOP) {
            library.addFirst(deadCard);
        } else {
            library.addLast(deadCard);
        }

        String ownerName = gameData.playerIdToName.get(ownerId);
        gameLogService.append(gameData, GameLog.builder()
                .card(deadCard)
                .text(" is put " + end + ownerName + "'s library.")
                .build());
        log.info("Game {} - {} puts {} {}{}'s library",
                gameData.id, entry.getCard().getName(), deadCard.getName(), end, ownerName);
    }
}
