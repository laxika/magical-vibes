package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DistinctManaValuesAmongCardsInGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "SNC", collectorNumber = "226")
public class SyndicateInfiltrator extends Card {

    public SyndicateInfiltrator() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new DistinctManaValuesAmongCardsInGraveyardAtLeast(5),
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
