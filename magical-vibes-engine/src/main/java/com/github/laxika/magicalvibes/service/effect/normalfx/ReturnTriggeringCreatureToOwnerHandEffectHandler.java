package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCreatureToOwnerHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Enduring Renewal resolution: return the dying creature card from the ability controller's
 * graveyard to their hand. Fizzles if the card left that graveyard in response (or was a token).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTriggeringCreatureToOwnerHandEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTriggeringCreatureToOwnerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTriggeringCreatureToOwnerHandEffect) effect;
        UUID dyingCardId = e.dyingCardId();
        UUID controllerId = entry.getControllerId();
        if (dyingCardId == null || controllerId == null) {
            return;
        }

        // "Return it to your hand" — only cards in the Enduring Renewal controller's graveyard.
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) {
            return;
        }
        Card creatureCard = null;
        for (Card card : graveyard) {
            if (card.getId().equals(dyingCardId)) {
                creatureCard = card;
                break;
            }
        }
        if (creatureCard == null) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (creature not in graveyard)."));
            log.info("Game {} - {} death trigger fizzles (creature card {} not in controller's graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCardId);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);
        gameData.playerHands.get(controllerId).add(creatureCard);

        String controllerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .card(creatureCard)
                .text(" returns from graveyard to " + controllerName + "'s hand.")
                .build());
        log.info("Game {} - {} returns {} from graveyard to {}'s hand",
                gameData.id, entry.getCard().getName(), creatureCard.getName(), controllerName);
    }
}
