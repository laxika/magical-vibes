package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByMatchingCreatureIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "WHO", collectorNumber = "170")
public class AcesBaseballBat extends Card {

    public AcesBaseballBat() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentIsAttackingPredicate(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.EQUIPPED_CREATURE),
                null));
        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentIsAttackingPredicate(),
                new MustBeBlockedByMatchingCreatureIfAbleEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.DALEK)),
                null));

        addActivatedAbility(new EquipActivatedAbility(
                "{1}",
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                "Target must be a legendary creature you control"));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
