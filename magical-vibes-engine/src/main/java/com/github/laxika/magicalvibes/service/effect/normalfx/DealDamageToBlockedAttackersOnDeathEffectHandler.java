package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
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

        Permanent dying = entry.getTriggeringPermanentSnapshot();
        if (dying == null) {
            return;
        }
        int damage = e.damage();
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;
        for (UUID blockedId : List.copyOf(dying.getBlockedAttackerIdsThisCombat())) {
            Permanent blocked = gameQueryService.findPermanentById(gameData, blockedId);
            if (blocked == null) continue;
            if (!damageSupport.isDamagePreventedForCreature(gameData, entry, blocked)) {
                damageSupport.dealCreatureDamage(gameData, entry, blocked, damage);
            }
        }
    }
}
