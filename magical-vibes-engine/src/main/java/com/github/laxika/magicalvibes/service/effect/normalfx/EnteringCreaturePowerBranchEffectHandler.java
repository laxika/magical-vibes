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
        Permanent enteringPermanent = entry.getTargetId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        int power = enteringPermanent != null
                ? gameQueryService.getEffectivePower(gameData, enteringPermanent)
                : entry.getSourcePermanentSnapshot() == null
                        ? Integer.MIN_VALUE
                        : entry.getSourcePermanentSnapshot().getEffectivePower();
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
}
