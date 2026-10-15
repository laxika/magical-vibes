package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DMU", collectorNumber = "286")
public class BriarHydra extends Card {

    public BriarHydra() {
        // Domain — Whenever this creature deals combat damage to a player, put X +1/+1 counters
        // on target creature you control, where X is the number of basic land types among lands
        // you control.
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new PutCounterOnTargetPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new BasicLandTypesAmongControlledLands()));
    }
}
