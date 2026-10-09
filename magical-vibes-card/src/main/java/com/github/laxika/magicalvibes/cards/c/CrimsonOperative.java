package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;


@CardRegistration(set = "MSH", collectorNumber = "126")
@CardRegistration(set = "MSC", collectorNumber = "848")
public class CrimsonOperative extends Card {

    public CrimsonOperative() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTopCardsMayPlayUntilNextTurnEffect(1));
    }
}
