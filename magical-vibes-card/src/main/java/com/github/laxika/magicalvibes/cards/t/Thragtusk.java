package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M13", collectorNumber = "193")
public class Thragtusk extends Card {

    public Thragtusk() {
        // When this creature enters, you gain 5 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(5));

        // When this creature leaves the battlefield, create a 3/3 green Beast creature token.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new CreateTokenEffect(
                1, "Beast", 3, 3, CardColor.GREEN, List.of(CardSubtype.BEAST),
                Set.of(), Set.of()
        ));
    }
}
