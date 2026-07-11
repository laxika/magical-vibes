package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ShuffleSelfFromGraveyardIntoLibraryEffect}: shuffles the source card from its
 * owner's graveyard into their library (e.g. Purity). Does nothing if the card has already left
 * the graveyard by the time the trigger resolves.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleSelfFromGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleSelfFromGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID ownerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard == null) return;

        boolean removed = graveyard.removeIf(c -> c.getId().equals(sourceCard.getId()));
        if (!removed) return;

        gameData.playerDecks.get(ownerId).add(sourceCard);
        LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        graveyardService.notifyCardsLeftGraveyard(gameData, ownerId);

        String playerName = gameData.playerIdToName.get(ownerId);
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " shuffles " + sourceCard.getName() + " into their library.");
        log.info("Game {} - {} shuffled into {}'s library", gameData.id, sourceCard.getName(), playerName);
    }
}
