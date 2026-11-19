package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "5DN", collectorNumber = "50")
public class FillWithFright extends Card {

    public FillWithFright() {
        addSpellEffect(new DiscardEffect(2, Subjects.targetPlayer()));
        addSpellEffect(new ScryEffect(2));
    }
}
