package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ShuffleSelfFromGraveyardIntoLibraryEffect}: shuffles the source card from its
 * owner's graveyard into their library (e.g. Purity). The owner still shuffles when the card has
 * already left the graveyard by the time the trigger resolves.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleSelfFromGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleSelfFromGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sourceCard = entry.getCard();
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, sourceCard.getId());
        if (ownerId == null) ownerId = sourceCard.getOwnerId() != null
                ? sourceCard.getOwnerId() : entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(ownerId);
        if (graveyard == null) return;

        boolean removed = graveyard.removeIf(c -> c.getId().equals(sourceCard.getId()));
        if (removed) gameData.playerDecks.get(ownerId).add(sourceCard);
        LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
        if (removed) graveyardService.notifyCardsLeftGraveyard(gameData, ownerId, sourceCard);

        String playerName = gameData.playerIdToName.get(ownerId);
        gameLogService.append(gameData, removed
                ? GameLog.textCardText(playerName + " shuffles ", sourceCard, " into their library.")
                : GameLog.text(playerName + " shuffles their library."));
        log.info("Game {} - {} shuffled into {}'s library", gameData.id, sourceCard.getName(), playerName);
    }
}
