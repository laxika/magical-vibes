package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves one-shot dynamic base-P/T setters: the amounts are determined once, as the effect resolves (CR 608.2h), and
 * become a fixed layer-7b setter (CR 613.4b) through {@link SetBasePowerToughnessEffectHandler}. A {@code null} amount
 * leaves that base value alone.
 */
@Component("setBasePowerToughnessToAmountNormalEffectHandler")
@RequiredArgsConstructor
public class SetBasePowerToughnessToAmountEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final SetBasePowerToughnessEffectHandler setBasePowerToughnessEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetBasePowerToughnessToAmountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SetBasePowerToughnessToAmountEffect setPT = (SetBasePowerToughnessToAmountEffect) effect;
        if (setPT.subject() instanceof PermanentSubject.Target) {
            // "Target creature has base power and toughness X/Y": amounts from the spell (Fractalize's X plus 1) or
            // from the ability's source as it is now, else as it last existed (Exuberant Wolfbear, CR 608.2h).
            Permanent liveSource = entry.getSourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            AmountContext context = AmountContext.forStackEntry(entry, liveSource);
            setBasePowerToughnessEffectHandler.resolve(gameData, entry,
                    new SetBasePowerToughnessEffect(evaluate(gameData, setPT.power(), context),
                            evaluate(gameData, setPT.toughness(), context), Subjects.target(),
                            setPT.duration() != null ? setPT.duration() : Duration.UNTIL_END_OF_TURN));
            return;
        }
        if (!(setPT.subject() instanceof PermanentSubject.Source)) {
            return;
        }
        Permanent source = subjectResolutionService.permanent(gameData, entry, effect, setPT.subject());
        if (source == null) {
            return;
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        setBasePowerToughnessEffectHandler.resolve(gameData, entry,
                new SetBasePowerToughnessEffect(evaluate(gameData, setPT.power(), context),
                        evaluate(gameData, setPT.toughness(), context), Subjects.source(),
                        setPT.duration() != null ? setPT.duration() : Duration.PERMANENT));
    }

    private Integer evaluate(GameData gameData, DynamicAmount amount, AmountContext context) {
        return amount == null ? null : amountEvaluationService.evaluate(gameData, amount, context);
    }
}
