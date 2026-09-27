package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;

@CardRegistration(set = "YMID", collectorNumber = "11")
public class SuntailSquadron extends Card {

    public SuntailSquadron() {
        addEffect(EffectSlot.SPELL,
                new ConjureCardNamedIntoHandEffect("Suntail Hawk", false, 7));
    }
}
