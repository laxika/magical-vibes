package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "EOE", collectorNumber = "56")
public class EmissaryEscort extends Card {

    public EmissaryEscort() {
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new GreatestManaValueAmongControlled(new PermanentIsArtifactPredicate(), true),
                new Fixed(0), GrantScope.SELF));
    }
}
