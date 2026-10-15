package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsAnotherPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "RIX", collectorNumber = "71")
public class FathomFleetBoarder extends Card {

    public FathomFleetBoarder() {
        // When this creature enters, you lose 2 life unless you control another Pirate.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ConditionalEffect.unless(
                new NotCondition(new ControlsAnotherPermanent(
                        new PermanentHasSubtypePredicate(CardSubtype.PIRATE))),
                new LoseLifeEffect(2)));
    }
}
