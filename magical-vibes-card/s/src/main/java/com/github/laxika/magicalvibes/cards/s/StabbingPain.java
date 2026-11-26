package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "M11", collectorNumber = "118")
public class StabbingPain extends Card {

    public StabbingPain() {
        addSpellEffect(BoostEffect.target(-1, -1));
        addSpellEffect(new TapPermanentsEffect(Subjects.target()));
    }
}
