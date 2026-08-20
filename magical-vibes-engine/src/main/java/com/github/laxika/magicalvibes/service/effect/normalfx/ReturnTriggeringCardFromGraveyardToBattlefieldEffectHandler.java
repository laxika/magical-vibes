package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Returns the triggering card from its owner's graveyard to the battlefield under its owner's
 * control, or under the triggered ability controller's control for Adarkar Valkyrie. Fizzles
 * silently if the card is no longer in a graveyard.
 */
@Component
@RequiredArgsConstructor
public class ReturnTriggeringCardFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTriggeringCardFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card card = entry.getCard();
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (ownerId == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " does nothing (the card is no longer in a graveyard)."));
            return;
        }

        boolean enterTapped = ((ReturnTriggeringCardFromGraveyardToBattlefieldEffect) effect).enterTapped();
        boolean returnUnderController = ((ReturnTriggeringCardFromGraveyardToBattlefieldEffect) effect)
                .returnUnderController();
        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
        UUID battlefieldControllerId = returnUnderController ? entry.getControllerId() : ownerId;
        graveyardReturnSupport.putCardOntoBattlefield(
                gameData, battlefieldControllerId, card, null, null, enterTapped);
    }
}
