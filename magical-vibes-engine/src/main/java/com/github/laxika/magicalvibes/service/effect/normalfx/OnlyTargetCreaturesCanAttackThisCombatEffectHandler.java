package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OnlyTargetCreaturesCanAttackThisCombatEffect;
import org.springframework.stereotype.Component;

/** Resolves an additional combat restriction to the chosen creature permanents. */
@Component
public class OnlyTargetCreaturesCanAttackThisCombatEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OnlyTargetCreaturesCanAttackThisCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.onlyPermanentsCanAttackThisCombatIds =
                ((OnlyTargetCreaturesCanAttackThisCombatEffect) effect).permanentIds();
    }
}
