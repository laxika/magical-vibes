package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "83")
public class Evacuation extends Card {

    public Evacuation() {
        addEffect(EffectSlot.SPELL, new ReturnCreaturesToOwnersHandEffect(Set.of()));
    }
}
