package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;

@CardRegistration(set = "10E", collectorNumber = "281")
@CardRegistration(set = "9ED", collectorNumber = "257")
@CardRegistration(set = "POR", collectorNumber = "176")
@CardRegistration(set = "P02", collectorNumber = "134")
public class NaturalSpring extends Card {

    public NaturalSpring() {
        addEffect(EffectSlot.SPELL, new TargetPlayerGainsLifeEffect(8));
    }
}
