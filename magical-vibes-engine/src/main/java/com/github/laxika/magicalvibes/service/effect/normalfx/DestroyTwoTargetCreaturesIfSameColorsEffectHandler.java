package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTwoTargetCreaturesIfSameColorsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DestroyTwoTargetCreaturesIfSameColorsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTwoTargetCreaturesIfSameColorsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> declaredTargetIds = entry.getDeclaredTargetIds();
        if (declaredTargetIds.size() != 2) {
            return;
        }

        List<Permanent> declaredTargets = new ArrayList<>(2);
        for (int i = 0; i < declaredTargetIds.size(); i++) {
            if (!entry.isTargetLegal(i)) {
                return;
            }
            UUID targetId = declaredTargetIds.get(i);
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                return;
            }
            declaredTargets.add(target);
        }

        Set<CardColor> firstColors = gameQueryService.getEffectiveColors(gameData, declaredTargets.getFirst());
        Set<CardColor> secondColors = gameQueryService.getEffectiveColors(gameData, declaredTargets.get(1));
        if (!firstColors.equals(secondColors)) {
            return;
        }

        List<Permanent> legalTargets = entry.getTargetIds().stream()
                .map(targetId -> gameQueryService.findPermanentById(gameData, targetId))
                .filter(java.util.Objects::nonNull)
                .toList();
        destructionSupport.destroyBatchCollecting(gameData, legalTargets, entry.getCard().getName(), true);
    }
}
