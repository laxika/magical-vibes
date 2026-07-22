package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "INR", collectorNumber = "144")
public class BloodPetalCelebrant extends Card {

    public BloodPetalCelebrant() {
        // This creature has first strike as long as it's attacking.
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF, new PermanentIsAttackingPredicate()));

        // When this creature dies, create a Blood token.
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.ofBloodToken(1));
    }
}
