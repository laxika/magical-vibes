package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeAnExampleEffect;

@CardRegistration(set = "NCC", collectorNumber = "37")
@CardRegistration(set = "NCC", collectorNumber = "138")
public class MakeAnExample extends Card {

    public MakeAnExample() {
        addEffect(EffectSlot.SPELL, new MakeAnExampleEffect());
    }
}
