package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnchantedPlayerCreaturesEnterTappedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Applies player-Aura creature entry replacement effects. */
@Component
public class EnchantedPlayerCreaturesEnterTappedEffectHandler {

    private final GameQueryService gameQueryService;

    public EnchantedPlayerCreaturesEnterTappedEffectHandler(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    public void apply(GameData gameData, UUID enteringControllerId, Permanent enteringPermanent) {
        if (!gameQueryService.isCreature(gameData, enteringPermanent)) {
            return;
        }

        gameData.forEachPermanent((sourcePlayerId, source) -> {
            if (enteringPermanent.isTapped() || !enteringControllerId.equals(source.getAttachedTo())) {
                return;
            }
            boolean applies = source.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof EnchantedPlayerCreaturesEnterTappedEffect);
            if (applies) {
                enteringPermanent.tap();
            }
        });
    }
}
