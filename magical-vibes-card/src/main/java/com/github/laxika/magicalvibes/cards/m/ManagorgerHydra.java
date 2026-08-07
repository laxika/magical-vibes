package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;

import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "186")
public class ManagorgerHydra extends Card {

    public ManagorgerHydra() {
        // Whenever a player casts a spell, put a +1/+1 counter on this creature.
        // Empty color set = any spell, including colorless ones and the controller's own.
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(Set.of(), 1, false));
    }
}
