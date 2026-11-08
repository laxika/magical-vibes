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

import java.util.UUID;

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

        // Both powers are read as the ability resolves, each creature's last known one once it has left
        // the battlefield (CR 608.2h).
        Permanent enteringPermanent = entry.getTriggeringPermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        int enteringPower = enteringPermanent != null
                ? gameQueryService.getEffectivePower(gameData, enteringPermanent)
                : lastKnownPower(gameData, entry.getTriggeringPermanentId(),
                        entry.getTriggeringPermanentPowerAtTrigger(), null);

        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int sourcePower = sourcePermanent != null
                ? gameQueryService.getEffectivePower(gameData, sourcePermanent)
                : lastKnownPower(gameData, entry.getSourcePermanentId(), entry.getSourcePowerAtTrigger(),
                        entry.getSourcePermanentSnapshot());

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

    private static int lastKnownPower(GameData gameData, UUID permanentId, Integer recorded, Permanent snapshot) {
        if (permanentId != null) {
            for (Permanent departed : gameData.departedPermanentSnapshots.values()) {
                if (departed.getId().equals(permanentId) && departed.getLastKnownPower() != null) {
                    return departed.getLastKnownPower();
                }
            }
        }
        if (recorded != null) {
            return recorded;
        }
        return snapshot == null ? Integer.MIN_VALUE : snapshot.getEffectivePower();
    }
}
