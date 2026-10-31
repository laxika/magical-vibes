package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TeachEffect;

@CardRegistration(set = "MB2", collectorNumber = "329")
@CardRegistration(set = "MB2", collectorNumber = "566")
public class Pyromancy101 extends Card {

    public Pyromancy101() {
        addSpellEffect(new DealDamageToAnyTargetEffect(1));
        addSpellEffect(new TeachEffect());
    }
}
