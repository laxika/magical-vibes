package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "267")
public class VoldarenEstate extends Card {

    public VoldarenEstate() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}, Pay 1 life: Add one mana of any color. Spend this mana only to cast a Vampire spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(1),
                        AwardAnyColorManaEffect.forSpellSubtypes(1, Set.of(CardSubtype.VAMPIRE))
                ),
                "{T}, Pay 1 life: Add one mana of any color. Spend this mana only to cast a Vampire spell."
        ));

        // {5}, {T}: Create a Blood token. This ability costs {1} less to activate for each Vampire
        // you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new ReduceActivationCostEffect(new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE), CountScope.CONTROLLER)),
                        CreateTokenEffect.ofBloodToken(1)
                ),
                "{5}, {T}: Create a Blood token. This ability costs {1} less to activate for each Vampire you control."
        ));
    }
}
