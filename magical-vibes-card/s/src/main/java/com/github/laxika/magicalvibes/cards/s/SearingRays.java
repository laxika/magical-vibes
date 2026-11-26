package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.filter.PermanentHasChosenSpellColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "165")
public class SearingRays extends Card {

    public SearingRays() {
        addSpellEffect(new ChooseColorAtResolutionEffect());
        addSpellEffect(DealDamageEffect.to(Subjects.eachPlayer(),
                new PermanentCount(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentHasChosenSpellColorPredicate())),
                CountScope.CONTROLLER)));
        addSpellEffect(new ClearChosenColorEffect());
    }
}
