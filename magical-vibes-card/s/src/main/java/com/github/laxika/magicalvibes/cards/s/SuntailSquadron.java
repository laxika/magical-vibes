package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;

@CardRegistration(set = "YMID", collectorNumber = "11")
public class SuntailSquadron extends Card {

    public SuntailSquadron() {
        addSpellEffect(new ConjureCardNamedIntoHandEffect("JUD", "28", false, 7));
    }
}
