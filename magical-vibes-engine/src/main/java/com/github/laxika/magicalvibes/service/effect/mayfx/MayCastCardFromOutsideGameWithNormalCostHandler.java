package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardFromOutsideGameWithNormalCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.OutsideGameNormalCostCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an accepted normal-cost cast offer from outside the game. */
@Component
@RequiredArgsConstructor
public class MayCastCardFromOutsideGameWithNormalCostHandler implements MayEffectHandlerBean {

    private final OutsideGameNormalCostCastSupport outsideGameNormalCostCastSupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastCardFromOutsideGameWithNormalCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayCastCardFromOutsideGameWithNormalCostEffect effect = ability.effects().stream()
                .filter(MayCastCardFromOutsideGameWithNormalCostEffect.class::isInstance)
                .map(MayCastCardFromOutsideGameWithNormalCostEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (accepted && ability.targetCardId() != null) {
            gameData.pendingMayAbilities.removeIf(pending -> pending != ability
                    && pending.effects().stream().anyMatch(candidate ->
                    candidate instanceof MayCastCardFromOutsideGameWithNormalCostEffect other
                            && other.offerGroupId().equals(effect.offerGroupId())));
            outsideGameNormalCostCastSupport.castFromOutsideGameWithNormalCost(
                    gameData, player, ability.targetCardId());
            return;
        }

        gameLogService.append(gameData,
                GameLog.textCardText(player.getUsername() + " declines to cast ", ability.sourceCard(), "."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
