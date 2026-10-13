package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EnchantedPlayerLosesGameEffectHandler implements NormalEffectHandlerBean {

    private final TargetPlayerLosesGameEffectHandler targetPlayerLosesGameEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedPlayerLosesGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID enchantedPlayerId = entry.getTargetId();
        if (enchantedPlayerId == null && entry.getSourcePermanentId() != null) {
            Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            enchantedPlayerId = aura == null ? null : aura.getAttachedTo();
        }
        if (enchantedPlayerId != null) {
            targetPlayerLosesGameEffectHandler.resolve(gameData, entry,
                    new TargetPlayerLosesGameEffect(enchantedPlayerId));
        }
    }
}
