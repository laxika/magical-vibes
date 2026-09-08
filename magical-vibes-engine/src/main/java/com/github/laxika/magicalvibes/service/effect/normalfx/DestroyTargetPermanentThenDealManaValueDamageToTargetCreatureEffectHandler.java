package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenDealManaValueDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DestroyTargetPermanentThenDealManaValueDamageToTargetCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentThenDealManaValueDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetPermanentThenDealManaValueDamageToTargetCreatureEffect) effect;
        UUID destructionTargetId = entry.getTargetId();
        if (destructionTargetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, destructionTargetId);
        if (target == null) {
            return;
        }

        int manaValue = target.getCard().getManaValue();
        UUID damageTargetId = entry.targetsForGroup(e.damageTargetGroup()).stream().findFirst().orElse(null);

        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), false);

        if (damageTargetId == null) {
            return;
        }
        Permanent damageTarget = gameQueryService.findPermanentById(gameData, damageTargetId);
        if (damageTarget == null || !gameQueryService.isCreature(gameData, damageTarget)) {
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, manaValue, entry);
        if (!damageSupport.isDamagePreventedForCreature(gameData, entry, damageTarget)) {
            damageSupport.dealCreatureDamage(gameData, entry, damageTarget, damage);
        }
    }
}
