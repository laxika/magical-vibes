package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "198")
@CardRegistration(set = "TSR", collectorNumber = "385")
@CardRegistration(set = "C19", collectorNumber = "201")
public class SecretPlans extends Card {

    public SecretPlans() {
        addStaticEffect(new StaticBoostEffect(0, 1, GrantScope.OWN_CREATURES, new PermanentIsFaceDownPredicate()));
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.YOU_CONTROL), new DrawCardEffect());
    }
}
