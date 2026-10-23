package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroySourceAndTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroySourceAndTargetPermanentEffect}: the source permanent and the target are
 * destroyed in one batch. A side that is already gone is skipped; the other is still destroyed.
 */
@Component
@RequiredArgsConstructor
public class DestroySourceAndTargetPermanentEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroySourceAndTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> toDestroy = new ArrayList<>();
        UUID sourceId = entry.getSourcePermanentId();
        Permanent source = sourceId == null ? null : gameQueryService.findPermanentById(gameData, sourceId);
        if (source != null) {
            toDestroy.add(source);
        }
        UUID targetId = entry.getTargetId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target != null && target != source) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
            if (controllerId != null) {
                entry.getRemovedPermanentControllers().put(target.getId(), controllerId);
            }
            entry.rememberLastKnownPermanentCard(target.getId(), target.getCard());
            entry.getLastKnownPermanentPowers().put(target.getId(),
                    gameQueryService.getEffectivePower(gameData, target));
            toDestroy.add(target);
        }
        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), false);
    }
}
