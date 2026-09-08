package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeNextInstantSorceryUncounterableThisTurnEffect;

@CardRegistration(set = "TOR", collectorNumber = "104")
public class Overmaster extends Card {

    public Overmaster() {
        addEffect(EffectSlot.SPELL, new MakeNextInstantSorceryUncounterableThisTurnEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
