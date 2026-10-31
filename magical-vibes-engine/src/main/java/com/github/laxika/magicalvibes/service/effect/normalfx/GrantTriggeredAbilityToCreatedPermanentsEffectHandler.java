package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityToCreatedPermanentsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves {@link GrantTriggeredAbilityToCreatedPermanentsEffect} by attaching the granted trigger to each created permanent. */
@Component
@RequiredArgsConstructor
public class GrantTriggeredAbilityToCreatedPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantTriggeredAbilityToCreatedPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantTriggeredAbilityToCreatedPermanentsEffect) effect;
        for (UUID permanentId : entry.getCreatedPermanentIds()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                permanent.addPersistentAbility(grant.grantedAbility());
            }
        }
    }
}
