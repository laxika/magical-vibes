package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;

@CardRegistration(set = "OGW", collectorNumber = "37")
public class StoneHavenOutfitter extends Card {

    public StoneHavenOutfitter() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_OWN_CREATURES,
                new PermanentIsEquippedPredicate()));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentIsEquippedPredicate(), new DrawCardEffect(1)));
        addEffect(EffectSlot.ON_DEATH, new TriggeringPermanentConditionalEffect(
                new PermanentIsEquippedPredicate(), new DrawCardEffect(1)));
    }
}
