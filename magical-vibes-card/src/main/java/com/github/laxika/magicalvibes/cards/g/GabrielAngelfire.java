package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "76")
public class GabrielAngelfire extends Card {

    public GabrielAngelfire() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Flying", new GrantKeywordEffect(
                        Keyword.FLYING, GrantScope.SELF, GrantDuration.UNTIL_YOUR_NEXT_UPKEEP)),
                new ChooseOneEffect.ChooseOneOption("First strike", new GrantKeywordEffect(
                        Keyword.FIRST_STRIKE, GrantScope.SELF, GrantDuration.UNTIL_YOUR_NEXT_UPKEEP)),
                new ChooseOneEffect.ChooseOneOption("Trample", new GrantKeywordEffect(
                        Keyword.TRAMPLE, GrantScope.SELF, GrantDuration.UNTIL_YOUR_NEXT_UPKEEP)),
                new ChooseOneEffect.ChooseOneOption("Rampage 3", GrantEffectToTargetEffect.toSource(
                        EffectSlot.ON_BECOMES_BLOCKED, rampageThree(),
                        EffectDuration.UNTIL_CONTROLLERS_NEXT_UPKEEP))
        )));
    }

    private static BoostSelfEffect rampageThree() {
        Scaled bonus = new Scaled(new Sum(new CreaturesBlockingSource(), new Fixed(-1)), 3);
        return new BoostSelfEffect(bonus, bonus);
    }
}
