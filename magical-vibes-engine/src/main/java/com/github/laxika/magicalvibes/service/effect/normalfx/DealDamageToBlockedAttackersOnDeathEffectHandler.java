package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToBlockedAttackersOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToBlockedAttackersOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToBlockedAttackersOnDeathEffect) effect;

        int damage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;
        for (UUID targetId : entry.getTargetIds()) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) continue;
            if (!damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
                damageSupport.dealCreatureDamage(gameData, entry, target, damage);
            }
        }
    
    }
}
