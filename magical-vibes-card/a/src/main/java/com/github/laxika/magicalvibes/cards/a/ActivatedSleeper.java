package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyCreatureCardInGraveyardOnEnterEffect;

import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "24")
@CardRegistration(set = "DMC", collectorNumber = "74")
public class ActivatedSleeper extends Card {

    public ActivatedSleeper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CopyCreatureCardInGraveyardOnEnterEffect(
                        null, null, null, Set.of(CardSubtype.PHYREXIAN), null,
                        false, false, false, false, false, true));
    }
}
