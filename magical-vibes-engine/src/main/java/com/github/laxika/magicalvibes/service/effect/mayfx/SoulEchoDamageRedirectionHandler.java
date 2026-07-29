package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SoulEchoUpkeepEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Soul Echo's echo-counter decision — the targeted opponent chooses whether, until Soul Echo's
 * controller's next upkeep, each 1 damage that would be dealt to that controller instead removes an
 * echo counter from Soul Echo. On accept the flag is armed on the source permanent; declining leaves
 * damage dealt normally for the turn cycle.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SoulEchoDamageRedirectionHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SoulEchoUpkeepEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Permanent source = ability.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());

        if (accepted && source != null) {
            source.setEchoDamageRedirectionActive(true);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " chooses the echo-counter replacement for ", ability.sourceCard(), "."));
            log.info("Game {} - {} armed the echo-counter damage replacement on {}", gameData.id,
                    player.getUsername(), ability.sourceCard().getName());
        } else {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines the echo-counter replacement for ", ability.sourceCard(), "."));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
