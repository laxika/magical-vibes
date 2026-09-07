package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "WAR", collectorNumber = "99")
public class MassacreGirl extends Card {

    public MassacreGirl() {
        CardEffect weakenOtherCreatures = new BoostAllCreaturesEffect(-1, -1,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, weakenOtherCreatures);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RegisterDelayedCreatureDeathTriggerEffect(weakenOtherCreatures));
    }
}
