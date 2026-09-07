package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "124")
public class PathOfPeril extends Card {

    public PathOfPeril() {
        addCastingOption(AlternateHandCast.cleave("{4}{W}{B}", null));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForAlternateCost(),
                new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate())));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()),
                new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentMaxManaValuePredicate(2))))));
    }
}
