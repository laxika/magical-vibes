package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "SPM", collectorNumber = "180")
@CardRegistration(set = "SPM", collectorNumber = "206")
@CardRegistration(set = "OM1", collectorNumber = "181")
public class MultiversalPassage extends Card {

    public MultiversalPassage() {
        addStaticEffect(new ChooseBasicLandTypeOnEnterEffect());
        addStaticEffect(new MayPayLifeOrEntersTappedEffect(2));
        addStaticEffect(GrantSubtypeEffect.chosenSubtype(Subjects.source(), true, new PermanentIsLandPredicate()));
    }
}
