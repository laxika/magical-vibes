package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLandDealsDamageToTriggeringCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

@Component
public class TargetLandDealsDamageToTriggeringCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;

    public TargetLandDealsDamageToTriggeringCreatureEffectHandler(
            DamageSupport damageSupport, GameQueryService gameQueryService) {
        this.damageSupport = damageSupport;
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetLandDealsDamageToTriggeringCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (TargetLandDealsDamageToTriggeringCreatureEffect) effect;
        if (entry.targetsForGroup(0).isEmpty() || entry.getTriggeringPermanentId() == null) {
            return;
        }

        Permanent land = gameQueryService.findPermanentById(gameData, entry.targetsForGroup(0).getFirst());
        Permanent creature = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (land == null || creature == null
                || !gameQueryService.isLand(gameData, land)
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        int damage = gameQueryService.applyDamageMultiplier(gameData, damageEffect.damage(), entry);
        damageSupport.dealCreatureDamage(gameData, entry, creature, damage, land);
    }
}
