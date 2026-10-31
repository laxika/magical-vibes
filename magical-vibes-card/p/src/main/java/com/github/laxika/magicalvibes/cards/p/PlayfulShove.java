package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "BLB", collectorNumber = "145")
public class PlayfulShove extends Card {

    public PlayfulShove() {
        addSpellEffect(new DealDamageToAnyTargetEffect(1));
        addSpellEffect(new DrawCardEffect(1));
    }
}
