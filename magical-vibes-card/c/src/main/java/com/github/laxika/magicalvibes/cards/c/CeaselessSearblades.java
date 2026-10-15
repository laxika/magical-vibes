package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LRW", collectorNumber = "158")
public class CeaselessSearblades extends Card {

    public CeaselessSearblades() {
        // Whenever you activate an ability of an Elemental, this creature gets +1/+0 until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_ABILITY, new TriggeringPermanentConditionalEffect(
                new PermanentHasSubtypePredicate(CardSubtype.ELEMENTAL),
                new BoostSelfEffect(1, 0)));
    }
}
