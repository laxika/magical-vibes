package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "LEG", collectorNumber = "16")
public class GreatDefender extends Card {

    public GreatDefender() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(new Fixed(0), new TargetManaValue()));
    }
}
