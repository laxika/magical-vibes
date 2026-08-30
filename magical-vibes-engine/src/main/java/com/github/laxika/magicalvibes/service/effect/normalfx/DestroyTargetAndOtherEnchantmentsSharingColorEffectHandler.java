package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndOtherEnchantmentsSharingColorEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Leave No Trace's targeted Radiance destruction. */
@Component
@RequiredArgsConstructor
public class DestroyTargetAndOtherEnchantmentsSharingColorEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetAndOtherEnchantmentsSharingColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Set<CardColor> targetColors = gameQueryService.getEffectiveColors(gameData, target);
        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!gameQueryService.isEnchantment(gameData, permanent)) {
                return;
            }
            if (permanent.getId().equals(target.getId())
                    || gameQueryService.getEffectiveColors(gameData, permanent).stream()
                    .anyMatch(targetColors::contains)) {
                toDestroy.add(permanent);
            }
        });

        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), false);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
