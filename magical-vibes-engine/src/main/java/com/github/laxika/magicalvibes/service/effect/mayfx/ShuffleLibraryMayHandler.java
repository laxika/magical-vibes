package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "You may shuffle your library" — resolved directly without creating a stack entry (e.g. Ponder).
 * When the wrapped effect is a targeted shuffle ({@link ShuffleLibraryEffect#targetPlayer()}), the
 * deciding controller may have the targeted player shuffle their library (e.g. Portent).
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
        // Targeted shuffle (Portent): the deciding controller has the targeted player shuffle.
        UUID shuffleTargetId = ability.controllerId();
        if (!ability.effects().isEmpty()
                && ability.effects().get(0) instanceof ShuffleLibraryEffect sle
                && sle.targetPlayer() && ability.targetCardId() != null) {
            shuffleTargetId = ability.targetCardId();
        }
        String shuffleTargetName = gameData.playerIdToName.get(shuffleTargetId);

        if (accepted) {
            LibraryShuffleHelper.shuffleLibrary(gameData, shuffleTargetId);
            String logEntry = shuffleTargetName + " shuffles their library.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} shuffles their library ({})", gameData.id,
                    shuffleTargetName, ability.sourceCard().getName());
        } else {
            String logEntry = player.getUsername() + " chooses not to shuffle.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} declines shuffle ({})", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
