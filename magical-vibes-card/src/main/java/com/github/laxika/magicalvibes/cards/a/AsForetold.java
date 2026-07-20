package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "AKH", collectorNumber = "42")
public class AsForetold extends Card {

    public AsForetold() {
        // At the beginning of your upkeep, put a time counter on As Foretold.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.TIME));

        // Once each turn, you may pay {0} rather than pay the mana cost for a spell you cast with
        // mana value X or less, where X is the number of time counters on As Foretold.
        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect("{0}", null, CounterType.TIME, true));
    }
}
