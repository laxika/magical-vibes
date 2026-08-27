package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithNormalCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileReducedCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an accepted normal-cost cast offer from source-linked exile. */
@Component
@RequiredArgsConstructor
public class MayCastExiledCardWithNormalCostHandler implements MayEffectHandlerBean {

    private final ExileReducedCastSupport exileReducedCastSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastExiledCardWithNormalCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayCastExiledCardWithNormalCostEffect effect = ability.effects().stream()
                .filter(MayCastExiledCardWithNormalCostEffect.class::isInstance)
                .map(MayCastExiledCardWithNormalCostEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (accepted && ability.targetCardId() != null) {
            gameData.pendingMayAbilities.removeIf(pending -> pending != ability
                    && pending.effects().stream()
                    .anyMatch(candidate -> candidate instanceof MayCastExiledCardWithNormalCostEffect other
                            && other.offerGroupId().equals(effect.offerGroupId())));
            exileReducedCastSupport.castFromExileWithNormalCostDuringResolution(
                    gameData, player, ability.targetCardId());
            return;
        }

        gameLogService.append(gameData,
                GameLog.textCardText(player.getUsername() + " declines to cast ", ability.sourceCard(), "."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
