package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "KHM", collectorNumber = "386")
public class RenegadeReaper extends Card {

    public RenegadeReaper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillControllerThenIfMilledEffect(
                4,
                new CardSubtypePredicate(CardSubtype.ANGEL),
                new GainLifeEffect(4)));
    }
}
