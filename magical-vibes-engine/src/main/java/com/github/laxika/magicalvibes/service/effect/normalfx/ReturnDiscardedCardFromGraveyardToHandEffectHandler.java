package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDiscardedCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Returns the discarded card only while it remains the same object in the controller's graveyard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnDiscardedCardFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnDiscardedCardFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID discardedCardId = entry.getTriggeringCardId();
        if (discardedCardId == null) {
            return;
        }

        if (gameData.graveyardEntryVersion(discardedCardId)
                != entry.getTriggeringCardGraveyardEntryVersion()) {
            return;
        }
        UUID ownerId = entry.getControllerId();
        Card discardedCard = gameData.playerGraveyards.getOrDefault(ownerId, List.of()).stream()
                .filter(card -> discardedCardId.equals(card.getId()))
                .findFirst().orElse(null);
        if (discardedCard == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, discardedCardId);
        gameData.addCardToHand(ownerId, discardedCard);

        String ownerName = gameData.playerIdToName.get(ownerId);
        gameLogService.append(gameData, GameLog.builder()
                .card(discardedCard)
                .text(" returns from graveyard to " + ownerName + "'s hand.")
                .build());
        log.info("Game {} - {} returns discarded card {} from graveyard to {}'s hand",
                gameData.id, entry.getCard().getName(), discardedCard.getName(), ownerName);
    }
}
