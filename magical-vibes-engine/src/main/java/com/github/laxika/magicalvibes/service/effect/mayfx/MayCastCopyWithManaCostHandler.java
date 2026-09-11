package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCopyWithManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileNormalCostCopySupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the optional cast of a copy with a fixed mana cost. */
@Component
@RequiredArgsConstructor
public class MayCastCopyWithManaCostHandler implements MayEffectHandlerBean {

    private final ExileNormalCostCopySupport exileNormalCostCopySupport;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastCopyWithManaCostEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            MayCastCopyWithManaCostEffect effect = ability.effects().stream()
                    .filter(MayCastCopyWithManaCostEffect.class::isInstance)
                    .map(MayCastCopyWithManaCostEffect.class::cast)
                    .findFirst()
                    .orElseThrow();
            exileNormalCostCopySupport.offerCast(gameData, player, ability.sourceCard(), effect.manaCost());
            return;
        }

        gameData.removeFromExile(ability.sourceCard().getId());
        gameLogService.append(gameData,
                GameLog.textCardText(player.getUsername() + " declines to cast the copy of ",
                        ability.sourceCard(), "."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
