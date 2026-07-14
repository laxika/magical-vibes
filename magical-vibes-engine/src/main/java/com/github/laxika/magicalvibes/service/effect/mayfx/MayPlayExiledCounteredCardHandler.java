package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCounteredCardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Play-exiled-countered-card-without-paying — e.g. Guile. Declining leaves the card exiled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayPlayExiledCounteredCardHandler implements MayEffectHandlerBean {

    private final ExileFreeCastSupport exileFreeCastSupport;
    private final GameBroadcastService gameBroadcastService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPlayExiledCounteredCardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted && ability.targetCardId() != null) {
            exileFreeCastSupport.castFromExileWithoutPaying(gameData, player, ability.targetCardId());
        } else {
            String logEntry = player.getUsername() + " declines to play " + ability.sourceCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} declines to play exiled {} (Guile)", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
