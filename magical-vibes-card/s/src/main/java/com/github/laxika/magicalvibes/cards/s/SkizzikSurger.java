package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "FUT", collectorNumber = "120")
public class SkizzikSurger extends Card {

    public SkizzikSurger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RegisterEchoAtNextUpkeepEffect(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsLandPredicate())));
    }
}
