package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MOR", collectorNumber = "79")
public class Stenchskipper extends Card {

    public Stenchskipper() {
        // At the beginning of the end step, if you control no Goblins, sacrifice this creature.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new NoOtherPermanent(new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)),
                new SacrificeSelfEffect()));
    }
}
