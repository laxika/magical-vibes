package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent enchanted = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (enchanted == null || !gameQueryService.isCreature(gameData, enchanted)) {
            return;
        }

        var boost = (BoostEnchantedAndSharingCreaturesUntilEndOfTurnEffect) effect;
        List<Permanent> toBoost = new ArrayList<>();
        gameData.forEachPermanent((ignored, permanent) -> {
            if (gameQueryService.isCreature(gameData, permanent)
                    && (permanent.getId().equals(enchanted.getId())
                    || gameQueryService.shareCreatureType(gameData, enchanted, permanent))) {
                toBoost.add(permanent);
            }
        });

        for (Permanent permanent : toBoost) {
            permanent.setPowerModifier(permanent.getPowerModifier() + boost.powerBoost());
            permanent.setToughnessModifier(permanent.getToughnessModifier() + boost.toughnessBoost());
        }
    }
}
