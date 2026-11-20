package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TDC", collectorNumber = "29")
@CardRegistration(set = "TDC", collectorNumber = "69")
public class TipTheScales extends Card {

    public TipTheScales() {
        addSpellEffect(SequenceEffect.of(
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.you()),
                new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(
                        new BoostAllCreaturesEffect(
                        new Scaled(AmountThisWay.sacrificedToughness(), -1),
                        new Scaled(AmountThisWay.sacrificedToughness(), -1))))));
    }
}
