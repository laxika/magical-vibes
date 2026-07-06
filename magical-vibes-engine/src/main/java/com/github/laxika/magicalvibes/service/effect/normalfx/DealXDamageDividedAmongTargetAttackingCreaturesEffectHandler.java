package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedAmongTargetAttackingCreaturesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealXDamageDividedAmongTargetAttackingCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealXDamageDividedAmongTargetAttackingCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        Map<UUID, Integer> assignments = entry.getDamageAssignments();
        if (assignments == null || assignments.isEmpty()) {
            return;
        }

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        List<Permanent> destroyed = new ArrayList<>();

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            Permanent target = gameQueryService.findPermanentById(gameData, assignment.getKey());
            if (target == null) continue;
            if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, target, entry.getCard())) continue;

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, assignment.getValue(), entry);
            if (damageSupport.dealCreatureDamage(gameData, entry, target, rawDamage)) {
                destroyed.add(target);
            }
        }

        damageSupport.destroyAllLethal(gameData, destroyed);
    
    }
}
