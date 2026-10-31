package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardTwoUnlessCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ECL", collectorNumber = "79")
public class ThirstForIdentity extends Card {

    public ThirstForIdentity() {
        addSpellEffect(new DrawCardEffect(3));
        addSpellEffect(new DiscardTwoUnlessCreatureEffect());
    }
}
