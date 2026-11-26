package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FUT", collectorNumber = "68")
@CardRegistration(set = "DDD", collectorNumber = "51")
@CardRegistration(set = "GVL", collectorNumber = "51")
@CardRegistration(set = "TSR", collectorNumber = "119")
@CardRegistration(set = "DMR", collectorNumber = "90")
public class IchorSlick extends Card {

    public IchorSlick() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-3, -3));
        addCycling("{2}");
        addCastingOption(new MadnessCast("{3}{B}"));
    }
}
