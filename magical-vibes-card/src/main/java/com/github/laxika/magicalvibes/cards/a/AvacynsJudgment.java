package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CastForMadnessCost;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "SOI", collectorNumber = "145")
public class AvacynsJudgment extends Card {

    public AvacynsJudgment() {
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(
                new Sum(new FixedIfCondition(new CastForMadnessCost(), 0, 2), new XValue())));

        addCastingOption(new MadnessCast("{X}{R}"));
    }
}
