package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;

@CardRegistration(set = "MKM", collectorNumber = "47")
@CardRegistration(set = "MKM", collectorNumber = "341")
@CardRegistration(set = "MKM", collectorNumber = "379")
public class ConspiracyUnraveler extends Card {

    public ConspiracyUnraveler() {
        addEffect(EffectSlot.STATIC, AlternativeCostForSpellsEffect.collectEvidence(10, null));
    }
}
