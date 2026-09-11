package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "HOB", collectorNumber = "11")
public class EagleOfTheGreatShelf extends Card {

    public EagleOfTheGreatShelf() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true),
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true)));
    }
}
