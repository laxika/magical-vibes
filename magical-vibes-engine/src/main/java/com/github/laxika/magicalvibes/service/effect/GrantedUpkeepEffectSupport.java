package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Collects upkeep-triggered abilities granted continuously via {@code GrantEffectEffect}
 * (e.g. Breath of Dreams granting {@link CumulativeUpkeepEffect} to green creatures) and via
 * {@link GrantTriggeredAbilityEffect} with an {@link EffectSlot#UPKEEP_TRIGGERED} slot (e.g.
 * Avarice Amulet granting its equipped creature "At the beginning of your upkeep, draw a card").
 */
@Component
@RequiredArgsConstructor
public class GrantedUpkeepEffectSupport {

    private final GameQueryService gameQueryService;

    public void appendGrantedUpkeepEffects(GameData gameData, Permanent permanent, List<CardEffect> into) {
        for (CardEffect granted : gameQueryService.computeStaticBonus(gameData, permanent).grantedEffects()) {
            if (granted instanceof CumulativeUpkeepEffect) {
                into.add(granted);
            } else if (granted instanceof GrantTriggeredAbilityEffect grant
                    && grant.slot() == EffectSlot.UPKEEP_TRIGGERED) {
                into.add(grant.grantedEffect());
            }
        }
    }
}
