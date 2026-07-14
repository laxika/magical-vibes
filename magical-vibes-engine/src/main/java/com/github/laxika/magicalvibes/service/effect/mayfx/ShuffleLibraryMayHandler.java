package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "You may shuffle your library" — resolved directly without creating a stack entry (e.g. Ponder).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleLibraryMayHandler implements MayEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleLibraryEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            LibraryShuffleHelper.shuffleLibrary(gameData, ability.controllerId());
            String logEntry = player.getUsername() + " shuffles their library.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} shuffles their library ({})", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());
        } else {
            String logEntry = player.getUsername() + " chooses not to shuffle.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines shuffle ({})", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
