package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ODY", collectorNumber = "305")
public class OtarianJuggernaut extends Card {

    public OtarianJuggernaut() {
        addStaticEffect(new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentHasSubtypePredicate(CardSubtype.WALL)));

        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);
        addStaticEffect(new ConditionalEffect(threshold,
                new StaticBoostEffect(3, 0, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(threshold,
                new MustAttackEffect()));
    }
}
