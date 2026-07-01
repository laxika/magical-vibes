package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "176")
public class BlechLoafingPest extends Card {

    public BlechLoafingPest() {
        // Whenever you gain life, put a +1/+1 counter on each Pest, Bat, Insect, Snake, and Spider you control.
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new PutPlusOnePlusOneCounterOnEachControlledPermanentEffect(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.PEST),
                        new PermanentHasSubtypePredicate(CardSubtype.BAT),
                        new PermanentHasSubtypePredicate(CardSubtype.INSECT),
                        new PermanentHasSubtypePredicate(CardSubtype.SNAKE),
                        new PermanentHasSubtypePredicate(CardSubtype.SPIDER)
                ))
        ));
    }
}
