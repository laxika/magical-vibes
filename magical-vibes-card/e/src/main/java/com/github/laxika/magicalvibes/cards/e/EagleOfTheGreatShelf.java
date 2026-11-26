package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "11")
public class EagleOfTheGreatShelf extends Card {

    public EagleOfTheGreatShelf() {
        addTrigger(Triggers.attacks(), BoostEffect.self(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true),
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true)));
    }
}
