package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

@CardRegistration(set = "PIP", collectorNumber = "37")
@CardRegistration(set = "PIP", collectorNumber = "329")
@CardRegistration(set = "PIP", collectorNumber = "565")
@CardRegistration(set = "PIP", collectorNumber = "857")
public class Radstorm extends Card {

    public Radstorm() {
        // Proliferate.
        addEffect(EffectSlot.SPELL, new ProliferateEffect());

        // Storm (When you cast this spell, copy it for each spell cast before it this turn.)
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
