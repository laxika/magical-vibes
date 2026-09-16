package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves one-shot dynamic base-P/T setters, such as Geometric Weird's end-step ability. */
@Component("setBasePowerToughnessToAmountNormalEffectHandler")
@RequiredArgsConstructor
public class SetBasePowerToughnessToAmountEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SetBasePowerToughnessEffectHandler setBasePowerToughnessEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetBasePowerToughnessToAmountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SetBasePowerToughnessToAmountEffect setPT = (SetBasePowerToughnessToAmountEffect) effect;
        if (setPT.scope() != GrantScope.SELF) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        AmountContext amountContext = AmountContext.forStackEntry(entry, source);
        int power = amountEvaluationService.evaluate(gameData, setPT.power(), amountContext);
        int toughness = amountEvaluationService.evaluate(gameData, setPT.toughness(), amountContext);
        setBasePowerToughnessEffectHandler.resolve(gameData, entry,
                new SetBasePowerToughnessEffect(power, toughness, GrantScope.SELF,
                        EffectDuration.PERMANENT));
    }
}
