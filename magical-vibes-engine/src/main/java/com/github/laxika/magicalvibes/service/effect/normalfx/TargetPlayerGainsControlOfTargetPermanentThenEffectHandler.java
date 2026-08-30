package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a permanent control change and queues its follow-up only after the change succeeds. */
@Component
@RequiredArgsConstructor
public class TargetPlayerGainsControlOfTargetPermanentThenEffectHandler implements NormalEffectHandlerBean {

    private final TargetPlayerGainsControlOfTargetPermanentEffectHandler controlHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerGainsControlOfTargetPermanentThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var controlThen = (TargetPlayerGainsControlOfTargetPermanentThenEffect) effect;
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

        UUID previousControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        controlHandler.resolve(gameData, entry, new TargetPlayerGainsControlOfTargetPermanentEffect());
        UUID currentControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (previousControllerId == null || previousControllerId.equals(newControllerId)
                || !newControllerId.equals(currentControllerId)) {
            return;
        }

        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0 && gameData.pendingEffectResolutionEntry == entry) {
            int pendingIndex = gameData.pendingEffectResolutionIndex;
            if (pendingIndex >= 0 && pendingIndex < entry.getEffectsToResolve().size()
                    && entry.getEffectsToResolve().get(pendingIndex) instanceof MayEffect may
                    && may.wrapped().equals(effect)) {
                effectIndex = pendingIndex;
            }
        }
        if (effectIndex < 0) {
            throw new IllegalStateException(
                    "TargetPlayerGainsControlOfTargetPermanentThenEffect is not part of the resolving entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1,
                List.of(new QueueReflexiveAbilityEffect(controlThen.thenEffect())));
    }
}
