package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TargetPlayerGainsControlOfTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerGainsControlOfTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var transfer = (TargetPlayerGainsControlOfTargetPermanentEffect) effect;
        List<UUID> targets = entry.getTargetIds();
        if (targets == null || targets.size() < 2) {
            return;
        }

        UUID newControllerId = targets.getFirst();
        if (!gameData.playerIds.contains(newControllerId)) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targets.get(1));
        if (target == null) {
            return;
        }

        UUID currentControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (currentControllerId == null) {
            return;
        }
        // Gaining control of a permanent you already control still stamps a new indefinite control
        // effect, which outlasts any limited-duration control effect over it (Bazaar Trader's ruling).
        boolean controlApplied = creatureControlService.applyControlEffect(
                gameData,
                newControllerId,
                target,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                EffectDuration.PERMANENT,
                null,
                entry.getCard().getName());
        boolean controlChanged = controlApplied && !newControllerId.equals(currentControllerId);

        if (controlChanged && transfer.thenEffect() != null) {
            CardEffect thenEffect = transfer.thenEffect();
            EffectHandler handler = effectHandlerRegistry.getHandler(thenEffect);
            if (handler != null) {
                handler.resolve(gameData, entry, thenEffect);
            }
        }
    }
}
