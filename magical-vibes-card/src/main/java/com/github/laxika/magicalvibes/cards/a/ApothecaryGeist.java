package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "INR", collectorNumber = "10")
public class ApothecaryGeist extends Card {

    public ApothecaryGeist() {
        // When this creature enters, if you control another Spirit, you gain 3 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new ControlsAnotherPermanent(new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)), new GainLifeEffect(3)));
    }
}
