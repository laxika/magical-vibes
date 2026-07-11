package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "109")
public class TaureanMauler extends Card {

    public TaureanMauler() {
        // Changeling is auto-loaded from Scryfall.
        // Whenever an opponent casts a spell, you may put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(null, List.of(new PutCountersOnSourceEffect(1, 1, 1))),
                "Put a +1/+1 counter on Taurean Mauler?"
        ));
    }
}
