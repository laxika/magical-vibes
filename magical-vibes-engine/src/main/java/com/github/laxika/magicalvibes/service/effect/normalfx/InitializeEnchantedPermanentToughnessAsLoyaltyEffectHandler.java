package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.InitializeEnchantedPermanentToughnessAsLoyaltyEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Captures the enchanted creature's starting toughness for a toughness-as-loyalty Aura. */
@Component
@RequiredArgsConstructor
public class InitializeEnchantedPermanentToughnessAsLoyaltyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return InitializeEnchantedPermanentToughnessAsLoyaltyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !source.isAttached()) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        if (target != null) {
            target.setToughnessAsLoyalty(target.getBaseToughness());
        }
    }
}
