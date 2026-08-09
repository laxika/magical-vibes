package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.PutTopCardOfGraveyardOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Moves the last card added to the controller's graveyard (its "top" card) to the bottom of that
 * player's library. A missing or empty graveyard resolves as a no-op.
 */
@Component
@RequiredArgsConstructor
public class PutTopCardOfGraveyardOnBottomOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTopCardOfGraveyardOnBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }

        Card topCard = graveyard.removeLast();
        graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, topCard);
        graveyardReturnSupport.moveCardToDestination(gameData, controllerId, topCard,
                GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY, null, null, false);
    }
}
