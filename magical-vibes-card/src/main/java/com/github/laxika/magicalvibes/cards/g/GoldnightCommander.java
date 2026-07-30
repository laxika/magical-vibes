package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "AVR", collectorNumber = "22")
public class GoldnightCommander extends Card {

    public GoldnightCommander() {
        // Whenever another creature you control enters, creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new BoostAllOwnCreaturesEffect(1, 1));
    }
}
