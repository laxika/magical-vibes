package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

@CardRegistration(set = "KTK", collectorNumber = "198")
public class SecretPlans extends Card {

    public SecretPlans() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(0, 1, GrantScope.OWN_CREATURES, new PermanentIsFaceDownPredicate()));
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP, new DrawCardEffect());
    }
}
