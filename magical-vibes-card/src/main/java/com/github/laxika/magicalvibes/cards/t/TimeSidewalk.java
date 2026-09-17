package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromOpeningHandAndCreateTimeWalkTokenCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MB1", collectorNumber = "31")
public class TimeSidewalk extends Card {

    public TimeSidewalk() {
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new ExileSourceCardFromOpeningHandAndCreateTimeWalkTokenCardsEffect(),
                "Exile this card from your opening hand?"
        ));
    }
}
