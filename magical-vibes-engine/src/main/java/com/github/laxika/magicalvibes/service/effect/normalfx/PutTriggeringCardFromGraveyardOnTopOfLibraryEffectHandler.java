package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringCardFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a death trigger that puts the triggering card on top of its controller's library. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTriggeringCardFromGraveyardOnTopOfLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTriggeringCardFromGraveyardOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID dyingCardId = ((PutTriggeringCardFromGraveyardOnTopOfLibraryEffect) effect).dyingCardId();
        UUID playerId = entry.getControllerId();
        if (dyingCardId == null || playerId == null) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        if (graveyard == null) {
            return;
        }

        Card deadCard = null;
        for (Card card : graveyard) {
            if (card.getId().equals(dyingCardId)) {
                deadCard = card;
                break;
            }
        }
        if (deadCard == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} death trigger fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCardId);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);
        gameData.playerDecks.get(playerId).addFirst(deadCard);

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.builder()
                .card(deadCard)
                .text(" is put on top of " + playerName + "'s library.")
                .build());
        log.info("Game {} - {} puts {} on top of {}'s library",
                gameData.id, entry.getCard().getName(), deadCard.getName(), playerName);
    }
}
