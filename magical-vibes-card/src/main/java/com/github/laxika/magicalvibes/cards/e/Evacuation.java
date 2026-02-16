package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;

import java.util.Set;

public class Evacuation extends Card {

    public Evacuation() {
        addEffect(EffectSlot.SPELL, new ReturnCreaturesToOwnersHandEffect(Set.of()));
    }
}
