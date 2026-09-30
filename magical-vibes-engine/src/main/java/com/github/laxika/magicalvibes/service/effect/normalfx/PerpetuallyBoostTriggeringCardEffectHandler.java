package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTriggeringCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Records a perpetual power/toughness boost on the card that caused the surrounding trigger. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostTriggeringCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostTriggeringCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = entry.getTriggeringCardId();
        if (cardId == null) {
            return;
        }

        var boost = (PerpetuallyBoostTriggeringCardEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        AmountContext context = AmountContext.forStackEntry(entry, source);
        gameData.perpetualCardPowerToughnessModifiers.merge(
                cardId,
                new CardPowerToughnessModifier(
                        amountEvaluationService.evaluate(gameData, boost.powerBoost(), context),
                        amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), context)),
                (oldValue, newValue) -> new CardPowerToughnessModifier(
                        oldValue.power() + newValue.power(), oldValue.toughness() + newValue.toughness()));
    }
}
