package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentCost;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "246")
public class FoodChain extends Card {

    public FoodChain() {
        // Exile a creature you control: Add X mana of any one color, where X is 1 plus the exiled
        // creature's mana value. Spend this mana only to cast creature spells.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new ExilePermanentCost(new PermanentIsCreaturePredicate(), "a creature", true, true),
                        new AwardAnyColorManaEffect(
                                new Sum(new Fixed(1), new XValue()),
                                ManaSpendRestriction.CREATURE_SPELL_ONLY,
                                null,
                                false)),
                "Exile a creature you control: Add X mana of any one color, where X is 1 plus the exiled creature's mana value. Spend this mana only to cast creature spells."
        ));
    }
}
