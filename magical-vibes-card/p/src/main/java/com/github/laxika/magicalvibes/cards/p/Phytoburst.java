package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "DGM", collectorNumber = "46")
public class Phytoburst extends Card {

    public Phytoburst() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(5, 5));
    }
}
