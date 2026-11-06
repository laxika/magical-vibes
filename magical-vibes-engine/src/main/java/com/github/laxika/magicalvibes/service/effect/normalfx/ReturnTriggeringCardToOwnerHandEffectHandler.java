package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Enduring Renewal / Yomiji, Who Bars the Way resolution: return the stack entry's triggering card from the
 * graveyard that holds it, its owner's, to that player's hand. Fizzles if the card left that graveyard in
 * response (or was a token).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTriggeringCardToOwnerHandEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTriggeringCardToOwnerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID dyingCardId = entry.getTriggeringCardId();
        if (dyingCardId == null) {
            return;
        }
        if (!entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(dyingCardId))) {
            return;
        }

        UUID recipientId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        Card deadCard = recipientId == null ? null : gameData.playerGraveyards.get(recipientId).stream()
                .filter(card -> card.getId().equals(dyingCardId))
                .findFirst()
                .orElse(null);
        if (deadCard == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} graveyard return trigger fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCardId);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);
        permanentRemovalService.addCardToHandFromGraveyard(gameData, recipientId, recipientId, deadCard);

        String recipientName = gameData.playerIdToName.get(recipientId);
        gameLogService.append(gameData, GameLog.builder()
                .card(deadCard)
                .text(" returns from graveyard to " + recipientName + "'s hand.")
                .build());
        log.info("Game {} - {} returns {} from graveyard to {}'s hand",
                gameData.id, entry.getCard().getName(), deadCard.getName(), recipientName);
    }
}
