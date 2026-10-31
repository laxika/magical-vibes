package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "MSH", collectorNumber = "93")
public class DarkDeed extends Card {

    public DarkDeed() {
        addSpellEffect(new BoostTargetCreatureEffect(-4, -4));
    }
}
