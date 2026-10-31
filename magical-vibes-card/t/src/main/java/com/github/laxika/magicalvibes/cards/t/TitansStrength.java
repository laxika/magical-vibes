package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ORI", collectorNumber = "166")
@CardRegistration(set = "THS", collectorNumber = "145")
@CardRegistration(set = "2X2", collectorNumber = "128")
@CardRegistration(set = "EA1", collectorNumber = "11")
public class TitansStrength extends Card {

    public TitansStrength() {
        // Target creature gets +3/+1 until end of turn. Scry 1.
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(3, 1))
                .addSpellEffect(new ScryEffect(1));
    }
}
