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

        List<Set<CardColor>> targetColors = new ArrayList<>(2);
        for (UUID targetId : declaredTargetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            Set<CardColor> colors = target != null
                    ? gameQueryService.getEffectiveColors(gameData, target)
                    : entry.getLastKnownTargetColors().get(targetId);
            if (colors == null) {
                return;
            }
            targetColors.add(colors);
        }

        if (!targetColors.getFirst().equals(targetColors.get(1))) {
            return;
        }

        List<Permanent> legalTargets = entry.getTargetIds().stream()
                .map(targetId -> gameQueryService.findPermanentById(gameData, targetId))
                .filter(java.util.Objects::nonNull)
                .toList();
        destructionSupport.destroyBatchCollecting(gameData, legalTargets, entry.getCard().getName(), true);
    }
}
