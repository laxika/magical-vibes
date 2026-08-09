package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllAttackingCreaturesBlockedEffect;
import com.github.laxika.magicalvibes.service.combat.block.CombatBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves an effect that makes every current attacker blocked without adding blockers. */
@Component
@RequiredArgsConstructor
public class MakeAllAttackingCreaturesBlockedEffectHandler implements NormalEffectHandlerBean {

    private final CombatBlockService combatBlockService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeAllAttackingCreaturesBlockedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.forEachPermanent((ignored, permanent) -> {
            if (permanent.isAttacking()) {
                combatBlockService.makeAttackingCreatureBlockedWithoutBlockers(gameData, permanent);
            }
        });
    }
}
