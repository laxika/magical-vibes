package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "CMM", collectorNumber = "158")
public class FeastOfSuccession extends Card {

    public FeastOfSuccession() {
        addSpellEffect(new BoostAllCreaturesEffect(-4, -4));
        addSpellEffect(new BecomeMonarchEffect());
    }
}
