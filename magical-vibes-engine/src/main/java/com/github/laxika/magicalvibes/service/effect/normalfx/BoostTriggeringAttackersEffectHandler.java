package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttackingPermanentSnapshot;
import com.github.laxika.magicalvibes.model.effect.BoostTriggeringAttackersEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Gives the attackers bound when an attack trigger was created +P/+T until end of turn. */
@Component
@RequiredArgsConstructor
public class BoostTriggeringAttackersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostTriggeringAttackersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        BoostTriggeringAttackersEffect boost = (BoostTriggeringAttackersEffect) effect;
        for (AttackingPermanentSnapshot attacker : boost.attackingCreatures()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, attacker.permanentId());
            if (permanent == null || !gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            permanent.setPowerModifier(permanent.getPowerModifier() + boost.powerBoost());
            permanent.setToughnessModifier(permanent.getToughnessModifier() + boost.toughnessBoost());
        }
    }
}
