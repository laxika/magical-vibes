package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AKH", collectorNumber = "101")
@CardRegistration(set = "AKR", collectorNumber = "117")
public class NestOfScarabs extends Card {

    public NestOfScarabs() {
        // Whenever you put one or more -1/-1 counters on a creature, create that many 1/1 black Insect
        // creature tokens. Fires once per placement event (not per counter); the placed count is the
        // event value. Controller-restricted: only counters this card's controller puts fire it.
        addEffect(EffectSlot.ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTERS_ON_CREATURE,
                new CreateTokenEffect(new EventValue(), "Insect", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.INSECT), Set.of(), Set.of()));
    }
}
