package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;

@CardRegistration(set = "TDM", collectorNumber = "2")
public class AnafenzaUnyieldingLineage extends Card {

    public AnafenzaUnyieldingLineage() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new EndureEffect(2));
    }
}
