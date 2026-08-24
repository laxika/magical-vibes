package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Supplies cast-time damage assignments for divided-damage spells cast through alternate paths. */
@Component
@RequiredArgsConstructor
public class DealDividedDamageSupport {

    private final AmountEvaluationService amountEvaluationService;

    public int damageAssignedToSingleTarget(GameData gameData, List<CardEffect> effects,
                                            UUID controllerId, int xValue, boolean madness) {
        return effects.stream()
                .filter(DealDividedDamageEffect.class::isInstance)
                .map(DealDividedDamageEffect.class::cast)
                .findFirst()
                .map(effect -> amountEvaluationService.evaluate(gameData, effect.totalDamage(),
                        AmountContext.forCasting(controllerId, xValue, madness)))
                .orElse(xValue);
    }
}
