package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "192")
public class SaplingNursery extends Card {

    public SaplingNursery() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.FOREST), CountScope.CONTROLLER)));

        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new CreateTokenEffect("Treefolk", 3, 4, CardColor.GREEN,
                        List.of(CardSubtype.TREEFOLK), Set.of(Keyword.REACH), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new ExileSelfCost(),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.OWN_PERMANENTS,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.TREEFOLK),
                                        new PermanentHasSubtypePredicate(CardSubtype.FOREST))))
                ),
                "{1}{G}, Exile this enchantment: Treefolk and Forests you control gain indestructible until end of turn."
        ));
    }
}
