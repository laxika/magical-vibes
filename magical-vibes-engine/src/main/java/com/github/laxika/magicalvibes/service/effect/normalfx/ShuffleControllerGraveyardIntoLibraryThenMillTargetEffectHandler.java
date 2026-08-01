package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleControllerGraveyardIntoLibraryThenMillTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShuffleControllerGraveyardIntoLibraryThenMillTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleControllerGraveyardIntoLibraryThenMillTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        // Tokens in the graveyard cease to exist rather than travel (CR 111.7), so they neither
        // move nor count toward the mill.
        List<Card> moving = graveyardService.takeGraveyardCardsForZoneChange(gameData, controllerId);
        int count = moving.size();
        gameData.playerDecks.get(controllerId).addAll(moving);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        gameLogService.append(gameData, GameLog.text(count == 0
                ? controllerName + "'s graveyard is empty. Library is shuffled."
                : controllerName + " shuffles their graveyard (" + LibraryShuffleSupport.pluralCards(count)
                        + ") into their library."));

        if (entry.getTargetId() != null) {
            graveyardService.resolveMillPlayer(gameData, entry.getTargetId(), count);
        }
    }
}
