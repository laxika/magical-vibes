package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardAdvantageSpellCastTriggerEffect;

@CardRegistration(set = "MB2", collectorNumber = "318")
@CardRegistration(set = "MB2", collectorNumber = "554")
public class TooferKeeperOfTheFullGrip extends Card {

    public TooferKeeperOfTheFullGrip() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CardAdvantageSpellCastTriggerEffect());
    }
}
