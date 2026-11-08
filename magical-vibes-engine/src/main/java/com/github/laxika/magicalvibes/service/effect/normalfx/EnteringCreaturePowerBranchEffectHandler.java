package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreaturePowerBranchEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnteringCreaturePowerBranchEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnteringCreaturePowerBranchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EnteringCreaturePowerBranchEffect branchEffect = (EnteringCreaturePowerBranchEffect) effect;
        // "Its power" is the stack entry's triggering permanent's as the ability resolves, else as it last existed
        // on the battlefield (CR 608.2h).
        UUID enteringId = entry.getTriggeringPermanentId();
        Permanent enteringPermanent = enteringId == null ? null : gameQueryService.findPermanentById(gameData, enteringId);
        int power = enteringPermanent != null
                ? gameQueryService.getEffectivePower(gameData, enteringPermanent)
                : lastKnownPower(gameData, entry, enteringId);
        CardEffect branch = power >= branchEffect.minPower()
                ? branchEffect.powerAtLeast()
                : branchEffect.belowPower();
        if (branch == null) {
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(branch);
        if (handler != null) {
            handler.resolve(gameData, entry, branch);
        } else {
            log.warn("No handler for entering-creature power branch effect: {}",
                    branch.getClass().getSimpleName());
        }
    }

    private static int lastKnownPower(GameData gameData, StackEntry entry, UUID enteringId) {
        if (enteringId != null) {
            Integer recorded = entry.getLastKnownPermanentPowers().get(enteringId);
            if (recorded != null) {
                return recorded;
            }
            for (Permanent departed : gameData.departedPermanentSnapshots.values()) {
                if (departed.getId().equals(enteringId) && departed.getLastKnownPower() != null) {
                    return departed.getLastKnownPower();
                }
            }
        }
        return entry.getTriggeringPermanentPowerAtTrigger() == null
                ? Integer.MIN_VALUE : entry.getTriggeringPermanentPowerAtTrigger();
    }
}
