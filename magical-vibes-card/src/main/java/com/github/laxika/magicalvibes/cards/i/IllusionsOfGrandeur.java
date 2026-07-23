package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "ICE", collectorNumber = "79")
public class IllusionsOfGrandeur extends Card {

    public IllusionsOfGrandeur() {
        // Cumulative upkeep {2}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{2}"));

        // When this enchantment enters, you gain 20 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(20));

        // When this enchantment leaves the battlefield, you lose 20 life.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new LoseLifeEffect(20));
    }
}
