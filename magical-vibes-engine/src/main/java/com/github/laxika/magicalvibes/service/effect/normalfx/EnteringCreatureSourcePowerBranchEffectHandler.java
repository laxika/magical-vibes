package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureSourcePowerBranchEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnteringCreatureSourcePowerBranchEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnteringCreatureSourcePowerBranchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EnteringCreatureSourcePowerBranchEffect branchEffect =
                (EnteringCreatureSourcePowerBranchEffect) effect;

        Permanent enteringPermanent = entry.getTriggeringPermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        int enteringPower = enteringPermanent == null
                ? entry.getTriggeringPermanentPowerAtTrigger() == null
                        ? Integer.MIN_VALUE
                        : entry.getTriggeringPermanentPowerAtTrigger()
                : gameQueryService.getEffectivePower(gameData, enteringPermanent);

        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int sourcePower = sourcePermanent == null
                ? entry.getSourcePermanentSnapshot() == null
                        ? Integer.MIN_VALUE
                        : entry.getSourcePermanentSnapshot().getEffectivePower()
                : gameQueryService.getEffectivePower(gameData, sourcePermanent);

        CardEffect branch = enteringPower < sourcePower
                ? branchEffect.belowSourcePower()
                : branchEffect.sourcePowerAtLeast();
        if (branch == null) {
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(branch);
        if (handler != null) {
            handler.resolve(gameData, entry, branch);
        } else {
            log.warn("No handler for entering-creature/source-power branch effect: {}",
                    branch.getClass().getSimpleName());
        }
    }
}
