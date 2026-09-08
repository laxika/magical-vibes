package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.VerdantMasteryEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "146")
public class VerdantMastery extends Card {

    public VerdantMastery() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{G}"))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForAlternateCost(), new VerdantMasteryEffect(true)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()), new VerdantMasteryEffect(false)));
    }
}
