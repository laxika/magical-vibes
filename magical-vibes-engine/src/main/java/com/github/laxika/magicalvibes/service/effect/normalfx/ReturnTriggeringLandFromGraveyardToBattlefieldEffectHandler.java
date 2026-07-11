package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringLandFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Returns the triggering land (Sacred Ground) from the graveyard back to the battlefield under the
 * ability controller's control. The controller is the graveyard owner, so the land returns under its
 * owner's control. Fizzles silently if the land is no longer in a graveyard.
 */
@Component
@RequiredArgsConstructor
public class ReturnTriggeringLandFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTriggeringLandFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTriggeringLandFromGraveyardToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();

        Card landCard = gameQueryService.findCardInGraveyardById(gameData, e.landCardId());
        if (landCard == null) {
            gameBroadcastService.logAndBroadcast(gameData,
                    entry.getDescription() + " does nothing (the land is no longer in a graveyard).");
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, landCard.getId());
        graveyardReturnSupport.putCardOntoBattlefield(gameData, controllerId, landCard);
    }
}
