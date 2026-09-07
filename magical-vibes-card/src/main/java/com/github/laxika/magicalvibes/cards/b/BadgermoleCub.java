package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenCreatureTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;

@CardRegistration(set = "TLA", collectorNumber = "167")
public class BadgermoleCub extends Card {

    public BadgermoleCub() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EarthbendTargetLandEffect(1));
        addEffect(EffectSlot.ON_CONTROLLER_TAPS_CREATURE_FOR_MANA,
                new AddManaWhenCreatureTappedForManaEffect(ManaColor.GREEN));
    }
}
