package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveOneOrMoreCountersFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BOT", collectorNumber = "7")
public class ArceeSharpshooter extends Card {

    public ArceeSharpshooter() {
        setBackFaceCard(new ArceeAcrobaticCoupe());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{R}{W}"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new RemoveOneOrMoreCountersFromSourceCost(CounterType.PLUS_ONE_PLUS_ONE),
                        new DealDamageToTargetCreatureEffect(new XValue()),
                        new TransformSelfEffect()
                ),
                "{1}, Remove one or more +1/+1 counters from Arcee: It deals that much damage to target creature. Convert Arcee.",
                TargetFilters.creature()
        ).withXValue());
    }

    @Override
    public String getBackFaceClassName() {
        return "ArceeAcrobaticCoupe";
    }
}
