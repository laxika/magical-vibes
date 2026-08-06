package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.SetAllOwnCreaturesBasePowerToughnessEffect;

@CardRegistration(set = "GTC", collectorNumber = "213")
public class BiomassMutation extends Card {

    public BiomassMutation() {
        // Creatures you control have base power and toughness X/X until end of turn.
        addEffect(EffectSlot.SPELL, new SetAllOwnCreaturesBasePowerToughnessEffect(new XValue(), new XValue()));
    }
}
