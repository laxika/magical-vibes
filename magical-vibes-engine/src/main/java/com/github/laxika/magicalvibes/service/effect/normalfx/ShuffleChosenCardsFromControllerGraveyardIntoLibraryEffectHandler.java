package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleChosenCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves "you shuffle up to N cards from your graveyard into your library", chosen on resolution. */
@Component
@RequiredArgsConstructor
public class ShuffleChosenCardsFromControllerGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleChosenCardsFromControllerGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var shuffle = (ShuffleChosenCardsFromControllerGraveyardIntoLibraryEffect) effect;
        var state = gameData.graveyardTargetOperation;
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);

        if (state.resolutionTimeShuffleOwnGraveyardCardsChoiceMade) {
            List<UUID> chosenIds = state.resolutionTimeShuffleOwnGraveyardCardsChosenCardIds;
            state.resolutionTimeShuffleOwnGraveyardCardsResume = false;
            state.resolutionTimeShuffleOwnGraveyardCardsChoiceMade = false;
            state.resolutionTimeShuffleOwnGraveyardCardsChosenCardIds = null;
            gameData.rerunCurrentEffectAfterInteraction = false;

            List<Card> chosen = new ArrayList<>();
            if (graveyard != null && chosenIds != null) {
                for (UUID cardId : chosenIds) {
                    graveyard.stream().filter(card -> card.getId().equals(cardId)).findFirst()
                            .filter(card -> chosen.size() < shuffle.maxCount())
                            .ifPresent(chosen::add);
                }
                graveyard.removeAll(chosen);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, chosen);
            }
            gameData.playerDecks.get(controllerId).addAll(chosen);
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                    + " shuffles " + chosen.size() + " card" + (chosen.size() == 1 ? "" : "s")
                    + " from their graveyard into their library."));
            return;
        }

        if (graveyard == null || graveyard.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        state.resolutionTimeShuffleOwnGraveyardCardsResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, new ArrayList<>(graveyard),
                Math.min(shuffle.maxCount(), graveyard.size()), 0,
                entry.getCard().getName() + " — Choose up to " + shuffle.maxCount()
                        + " cards from your graveyard to shuffle into your library.");
    }
}
