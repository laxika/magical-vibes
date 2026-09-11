package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCopyWithNormalCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileNormalCostCopySupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the optional normal-cost cast of a copy created during resolution. */
@Component
@RequiredArgsConstructor
public class MayCastCopyWithNormalCostHandler implements MayEffectHandlerBean {

    private final ExileNormalCostCopySupport exileNormalCostCopySupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastCopyWithNormalCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            exileNormalCostCopySupport.offerCast(gameData, player, ability.sourceCard());
            return;
        }

        gameData.removeFromExile(ability.sourceCard().getId());
        gameLogService.append(gameData,
                GameLog.textCardText(player.getUsername() + " declines to cast the copy of ",
                        ability.sourceCard(), "."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
