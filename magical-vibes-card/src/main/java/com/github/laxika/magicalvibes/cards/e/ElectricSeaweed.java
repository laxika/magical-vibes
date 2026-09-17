package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "23")
public class ElectricSeaweed extends Card {

    public ElectricSeaweed() {
        // When this creature enters, until end of turn, whenever another creature dies, this
        // creature deals 1 damage to each non-Wall creature.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GrantEffectToSourceUntilEndOfTurnEffect(
                EffectSlot.ON_ANY_CREATURE_DIES,
                new DealDamageToEachMatchingPermanentEffect(
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(
                                        new PermanentHasSubtypePredicate(CardSubtype.WALL)))),
                        EachPermanentScope.ALL_PLAYERS)));

        // {T}: This creature deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Electric Seaweed deals 1 damage to any target."));
    }
}
