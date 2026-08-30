package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithCostReductionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileReducedCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayCastExiledCardWithCostReductionHandler implements MayEffectHandlerBean {

    private final ExileReducedCastSupport exileReducedCastSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastExiledCardWithCostReductionEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayCastExiledCardWithCostReductionEffect effect = ability.effects().stream()
                .filter(MayCastExiledCardWithCostReductionEffect.class::isInstance)
                .map(MayCastExiledCardWithCostReductionEffect.class::cast)
                .findFirst()
                .orElseThrow();
        if (accepted && ability.targetCardId() != null) {
            exileReducedCastSupport.castFromExileWithCostReduction(gameData, player,
                    ability.targetCardId(), effect.genericCostReduction());
        } else {
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines to cast ",
                    ability.sourceCard(), "."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
