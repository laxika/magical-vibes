package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "BOK", collectorNumber = "63")
public class CallForBlood extends Card {

    public CallForBlood() {
        // The sacrifice cost snapshots the sacrificed creature's power into the entry's xValue.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(false, true));
        // Target creature gets -X/-X until end of turn.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new Scaled(new XValue(), -1),
                new Scaled(new XValue(), -1)
        ));
    }
}
