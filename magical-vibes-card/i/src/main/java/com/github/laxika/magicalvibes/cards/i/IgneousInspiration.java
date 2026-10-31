package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;

@CardRegistration(set = "STX", collectorNumber = "107")
public class IgneousInspiration extends Card {

    public IgneousInspiration() {
        addSpellEffect(new DealDamageToAnyTargetEffect(3));
        addSpellEffect(new LearnEffect());
    }
}
