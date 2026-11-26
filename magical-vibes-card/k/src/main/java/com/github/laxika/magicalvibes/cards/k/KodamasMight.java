package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;

@CardRegistration(set = "CHK", collectorNumber = "224")
public class KodamasMight extends Card {

    public KodamasMight() {
        addSpellEffect(BoostEffect.target(2, 2));
        addStaticEffect(new SpliceEffect(CardSubtype.ARCANE, "{G}"));
    }
}
