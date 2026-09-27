package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ElrondOfTheWhiteCouncilEffect;

@CardRegistration(set = "LTC", collectorNumber = "51")
@CardRegistration(set = "LTC", collectorNumber = "134")
public class ElrondOfTheWhiteCouncil extends Card {

    public ElrondOfTheWhiteCouncil() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ElrondOfTheWhiteCouncilEffect());
    }
}
