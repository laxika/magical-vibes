package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "9ED", collectorNumber = "152")
@CardRegistration(set = "8ED", collectorNumber = "152")
@CardRegistration(set = "FDN", collectorNumber = "180")
@CardRegistration(set = "FDN", collectorNumber = "322")
@CardRegistration(set = "FDN", collectorNumber = "386")
@CardRegistration(set = "ONE", collectorNumber = "104")
@CardRegistration(set = "APC", collectorNumber = "47")
@CardRegistration(set = "HOP", collectorNumber = "36")
@CardRegistration(set = "DDE", collectorNumber = "27")
@CardRegistration(set = "MB1", collectorNumber = "144")
@CardRegistration(set = "HA1", collectorNumber = "8")
@CardRegistration(set = "C15", collectorNumber = "130")
public class PhyrexianArena extends Card {

    public PhyrexianArena() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
    }
}
