package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "M12", collectorNumber = "198")
@CardRegistration(set = "M13", collectorNumber = "195")
@CardRegistration(set = "M15", collectorNumber = "203")
@CardRegistration(set = "ORI", collectorNumber = "201")
@CardRegistration(set = "M19", collectorNumber = "205")
@CardRegistration(set = "M20", collectorNumber = "343")
@CardRegistration(set = "M21", collectorNumber = "210")
@CardRegistration(set = "ONE", collectorNumber = "187")
@CardRegistration(set = "WOE", collectorNumber = "191")
@CardRegistration(set = "ANB", collectorNumber = "106")
public class TitanicGrowth extends Card {

    public TitanicGrowth() {
        addSpellEffect(BoostEffect.target(4, 4));
    }
}
