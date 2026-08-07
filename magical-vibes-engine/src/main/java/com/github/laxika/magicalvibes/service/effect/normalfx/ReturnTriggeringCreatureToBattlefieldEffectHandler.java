package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCreatureToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Angelic Renewal resolution: return the dying creature card from the ability controller's graveyard
 * to the battlefield under their control. Fizzles if the card left that graveyard in response (or
 * was a token, which leaves no card behind).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTriggeringCreatureToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTriggeringCreatureToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID dyingCardId = ((ReturnTriggeringCreatureToBattlefieldEffect) effect).dyingCardId();
        UUID controllerId = entry.getControllerId();
        if (dyingCardId == null || controllerId == null) {
            return;
        }

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
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (creature not in graveyard)."));
            log.info("Game {} - {} death trigger fizzles (creature card {} not in controller's graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCardId);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);
        graveyardReturnSupport.putCardOntoBattlefield(gameData, controllerId, creatureCard);
    }
}
