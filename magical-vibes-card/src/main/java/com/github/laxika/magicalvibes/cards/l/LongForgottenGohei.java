package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "261")
public class LongForgottenGohei extends Card {

    public LongForgottenGohei() {
        // Arcane spells you cast cost {1} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.ARCANE), 1, CostModificationScope.SELF));

        // Spirit creatures you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, GrantScope.OWN_CREATURES, new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));
    }
}
