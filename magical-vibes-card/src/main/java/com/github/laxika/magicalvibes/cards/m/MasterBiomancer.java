package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithSourcePowerCountersEffect;

@CardRegistration(set = "GTC", collectorNumber = "176")
@CardRegistration(set = "2X2", collectorNumber = "250")
@CardRegistration(set = "C21", collectorNumber = "224")
@CardRegistration(set = "C16", collectorNumber = "210")
@CardRegistration(set = "CM2", collectorNumber = "159")
public class MasterBiomancer extends Card {

    public MasterBiomancer() {
        // Each other creature you control enters with a number of additional +1/+1 counters on it
        // equal to this creature's power and as a Mutant in addition to its other types.
        addEffect(EffectSlot.STATIC, new ControlledCreaturesEnterWithSourcePowerCountersEffect(CardSubtype.MUTANT));
    }
}
