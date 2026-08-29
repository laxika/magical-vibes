package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetDealsPowerDamageToTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetDealsPowerDamageToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetDealsPowerDamageToTargetEffect) effect;

        List<UUID> sourceGroup = entry.targetsForGroup(e.sourceTargetGroup());
        List<UUID> victimGroup = entry.targetsForGroup(e.victimTargetGroup());
        if (sourceGroup.isEmpty() || victimGroup.isEmpty()) {
            return; // Optional target not chosen ("up to one") — no damage is dealt
        }

        Permanent biter = gameQueryService.findPermanentById(gameData, sourceGroup.getFirst());
        Permanent target = gameQueryService.findPermanentById(gameData, victimGroup.getFirst());
        if (biter == null || target == null) {
            return;
        }

        // The biting creature deals the damage — check if it is prevented from dealing damage
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.isPreventedFromDealingDamage(gameData, biter)) {
            
            gameLogService.append(gameData, GameLog.cardThen(biter.getCard(), "'s damage is prevented."));
            return;
        }

        // Use the biting creature's color for protection checks (not the spell's color)
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, target, biter)) {
            gameLogService.append(gameData, GameLog.cardThen(biter.getCard(), "'s damage is prevented."));
            return;
        }

        int power = gameQueryService.getPowerBasedDamage(gameData, biter);
        int rawDamage = gameQueryService.applyDamageMultiplier(
                gameData, power * e.powerMultiplier(), entry);
        damageSupport.dealCreatureDamage(gameData, entry, target, rawDamage, biter);
    }
}
