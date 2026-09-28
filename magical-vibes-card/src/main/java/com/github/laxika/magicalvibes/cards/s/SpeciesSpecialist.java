package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;

@CardRegistration(set = "SLD", collectorNumber = "2369")
public class SpeciesSpecialist extends Card {

    public SpeciesSpecialist() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        var deathTrigger = new TriggeringPermanentConditionalEffect(
                new PermanentHasSourceChosenSubtypePredicate(),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
        addEffect(EffectSlot.ON_DEATH, deathTrigger);
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, deathTrigger);
    }
}
