package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.AddActivatedAbilityCostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DMU", collectorNumber = "259")
public class ThranPortal extends Card {

    public ThranPortal() {
        addStaticEffect(new ConditionalReplacementEffect(
                new ControlsPermanentCount(3, new PermanentIsLandPredicate()), new EntersTappedEffect()));
        addStaticEffect(new ChooseBasicLandTypeOnEnterEffect());
        addStaticEffect(GrantSubtypeEffect.chosenSubtype(Subjects.source(), false, new PermanentIsLandPredicate()));
        addStaticEffect(new AddActivatedAbilityCostEffect(
                new PermanentIsSourceCardPredicate(), new PayLifeCost(1)));
    }
}
