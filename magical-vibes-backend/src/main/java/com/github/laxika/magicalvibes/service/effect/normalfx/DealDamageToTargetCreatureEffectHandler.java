package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetCreatureEffect) effect;

        int damage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);

        // Multi-target: deal damage to each valid creature target (e.g. Dual Shot targeting two creatures,
        // Fire Shrine Keeper targeting up to two creatures via activated ability).
        // Enters this path when targetIds has 2+ entries, or when targetIds has 1 entry but targetId is null
        // (multi-target ability with a single target selected). Skips non-creature UUIDs (e.g. player
        // targets from kicked spells) since those are handled by other effects like DealDamageToSecondaryTargetEffect.
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()
                && (entry.getTargetIds().size() > 1 || entry.getTargetId() == null)) {
            for (UUID targetId : entry.getTargetIds()) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) continue;
                if (e.unpreventable()) {
                    damageSupport.dealDamageAndDestroyIfLethalUnpreventable(gameData, entry, target, damage);
                } else if (!damageSupport.isDamagePreventedForCreature(gameData, entry, target)) {
                    damageSupport.dealDamageAndDestroyIfLethal(gameData, entry, target, damage);
                }
            }
            return;
        }

        // Single-target
        if (e.unpreventable()) {
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target == null) return;
            damageSupport.dealDamageAndDestroyIfLethalUnpreventable(gameData, entry, target, damage);
        } else {
            damageSupport.resolveCreatureTargetDamage(gameData, entry, damage);
        }
    
    }
}
