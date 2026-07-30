package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Collects triggered abilities granted continuously via {@link GrantTriggeredAbilityEffect}
 * (e.g. Tandem Lookout granting its soulbond pair "Whenever this creature deals damage to an
 * opponent, draw a card"). Trigger-collection sites append these next to the permanent's own
 * {@code EffectSlot} effects.
 */
@Component
@RequiredArgsConstructor
public class GrantedTriggeredAbilitySupport {

    private final GameQueryService gameQueryService;

    public List<CardEffect> grantedTriggeredEffects(GameData gameData, Permanent permanent, EffectSlot slot) {
        List<CardEffect> result = new ArrayList<>();
        for (CardEffect granted : gameQueryService.computeStaticBonus(gameData, permanent).grantedEffects()) {
            if (granted instanceof GrantTriggeredAbilityEffect grant && grant.slot() == slot) {
                result.add(grant.grantedEffect());
            }
        }
        return result;
    }
}
