package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TeachCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileNormalCostCopySupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeachCastCopyHandler implements MayEffectHandlerBean {

    private static final String TEACH_COST = "{1}{R}";

    private final GameLogService gameLogService;
    private final ExileNormalCostCopySupport exileNormalCostCopySupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TeachCastCopyEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            gameData.removeFromExile(ability.sourceCard().getId());
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " declines to cast the copy of ",
                            ability.sourceCard(), "."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        exileNormalCostCopySupport.offerCast(gameData, player, ability.sourceCard(), TEACH_COST);
    }
}
