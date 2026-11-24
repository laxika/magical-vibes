package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "132")
@CardRegistration(set = "ECL", collectorNumber = "363")
@CardRegistration(set = "ECL", collectorNumber = "387")
@CardRegistration(set = "ECL", collectorNumber = "397")
public class CollectiveInferno extends Card {

    public CollectiveInferno() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addStaticEffect(DoubleDamageFromCreaturesEffect.fromSourcesYouControl(
                new PermanentHasSourceChosenSubtypePredicate(), new CardHasSourceChosenSubtypePredicate(false)));
    }
}
