package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "261")
@CardRegistration(set = "MM2", collectorNumber = "221")
public class LongForgottenGohei extends Card {

    public LongForgottenGohei() {
        // Arcane spells you cast cost {1} less to cast.
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.ARCANE), 1, CostModificationScope.SELF));

        // Spirit creatures you control get +1/+1.
        addStaticEffect(new StaticBoostEffect(
                1, 1, GrantScope.ALL_OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));
    }
}
