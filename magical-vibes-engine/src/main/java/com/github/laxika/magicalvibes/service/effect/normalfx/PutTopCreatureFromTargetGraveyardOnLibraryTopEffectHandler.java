package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTopCreatureFromTargetGraveyardOnLibraryTopEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * If the last card added to the target player's graveyard (its "top" card) is a creature, moves it
 * to the top of that player's library. Empty or non-creature top resolves as a no-op.
 */
@Component
@RequiredArgsConstructor
public class PutTopCreatureFromTargetGraveyardOnLibraryTopEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTopCreatureFromTargetGraveyardOnLibraryTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }

        Card topCard = graveyard.getLast();
        if (!topCard.hasType(CardType.CREATURE)) {
            return;
        }

        graveyard.removeLast();
        graveyardService.notifyCardsLeftGraveyard(gameData, targetPlayerId, topCard);
        graveyardReturnSupport.moveCardToDestination(gameData, targetPlayerId, topCard,
                GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY, null, null, false);
    }
}
