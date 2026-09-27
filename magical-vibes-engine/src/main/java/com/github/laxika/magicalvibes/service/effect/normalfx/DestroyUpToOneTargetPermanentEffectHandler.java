package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToOneTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyUpToOneTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyUpToOneTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> effectTargets = entry.targetsForEffect(effect);
        UUID targetId = effectTargets.isEmpty() ? entry.getTargetId() : effectTargets.getFirst();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId != null) {
            entry.getRemovedPermanentControllers().put(target.getId(), controllerId);
        }
        entry.rememberLastKnownPermanentCard(target.getId(), target.getCard());
        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), false);
    }
}
