package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "37")
public class StoneHavenOutfitter extends Card {

    public StoneHavenOutfitter() {
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES,
                new PermanentIsEquippedPredicate()));
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), new TriggeringPermanentConditionalEffect(
                new PermanentIsEquippedPredicate(), new DrawCardEffect(1)));
    }
}
