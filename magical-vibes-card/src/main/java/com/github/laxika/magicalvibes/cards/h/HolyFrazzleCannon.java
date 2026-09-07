package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCreatureTypeWithEquippedCreaturePredicate;

import java.util.List;

/** Back face of {@link com.github.laxika.magicalvibes.cards.i.InvasionOfNewCapenna}. */
public class HolyFrazzleCannon extends Card {

    public HolyFrazzleCannon() {
        addEffect(EffectSlot.ON_ATTACK,
                new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addEffect(EffectSlot.ON_ATTACK, new PutCounterOnEachMatchingPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                1,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentSharesCreatureTypeWithEquippedCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsHostOfSourceAuraPredicate()))),
                EachPermanentScope.ALL_PLAYERS));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
