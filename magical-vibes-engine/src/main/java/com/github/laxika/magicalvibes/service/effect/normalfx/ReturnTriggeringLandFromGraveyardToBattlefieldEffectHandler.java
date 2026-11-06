package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringLandFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Returns the lands the trigger event was about (the entry's event cards, or else its triggering card) from the
 * graveyard to the battlefield under the ability controller's control. The controller is the graveyard owner, so a
 * land returns under its owner's control. Does nothing for a land that is no longer that graveyard object.
 */
@Component
@RequiredArgsConstructor
public class ReturnTriggeringLandFromGraveyardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTriggeringLandFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTriggeringLandFromGraveyardToBattlefieldEffect) effect;
        List<Card> lands = new ArrayList<>();
        if (!entry.getEventCardIds().isEmpty()) {
            for (UUID cardId : entry.getEventCardIds()) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                Long version = entry.getEventCardGraveyardEntryVersions().get(cardId);
                if (card != null && (version == null || version == gameData.graveyardEntryVersion(cardId))) {
                    lands.add(card);
                }
            }
        } else if (entry.getTriggeringCardId() != null) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, entry.getTriggeringCardId());
            if (card != null && entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(card.getId()))) {
                lands.add(card);
            }
        }
        if (lands.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " does nothing (the land is no longer in a graveyard)."));
            return;
        }

        for (Card landCard : lands) {
            permanentRemovalService.removeCardFromGraveyardById(gameData, landCard.getId());
            graveyardReturnSupport.putCardOntoBattlefield(
                    gameData, entry.getControllerId(), landCard, null, null, e.enterTapped());
        }
    }
}
