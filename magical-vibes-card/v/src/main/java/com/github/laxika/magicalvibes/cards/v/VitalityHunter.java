package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "30")
public class VitalityHunter extends Card {

    public VitalityHunter() {
        SourceIsMonstrous monstrous = new SourceIsMonstrous();

        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{W}{W}",
                List.of(new MonstrosityEffect(new XValue())),
                "{X}{W}{W}: Monstrosity X."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));

        targetUpTo(new XValue(), TargetFilters.creature(), 99)
                .addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS,
                        new PutCounterOnTargetPermanentEffect(CounterType.LIFELINK));
    }
}
