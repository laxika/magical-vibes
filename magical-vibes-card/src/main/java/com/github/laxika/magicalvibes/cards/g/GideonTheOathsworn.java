package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "265")
public class GideonTheOathsworn extends Card {

    public GideonTheOathsworn() {
        PermanentPredicate nonGideonCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.GIDEON))));
        PermanentPredicate nonGideonAttacker = new PermanentAllOfPredicate(List.of(
                nonGideonCreature,
                new PermanentIsAttackingPredicate(),
                new PermanentControlledBySourceControllerPredicate()));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(
                        new MinimumMatchingAttackers(2, nonGideonCreature),
                        new PutCounterOnEachMatchingPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, nonGideonAttacker,
                                EachPermanentScope.ALL_PLAYERS)));

        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(
                        new AnimatePermanentsEffect(5, 5, List.of(CardSubtype.SOLDIER), Set.of(),
                                CardColor.WHITE),
                        PreventDamageEffect.allToSelf()),
                "+2: Until end of turn, Gideon, the Oathsworn becomes a 5/5 white Soldier creature "
                        + "that's still a planeswalker. Prevent all damage that would be dealt to him this turn."));

        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(
                        new ExileAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))),
                        new ExileSelfEffect()),
                "-9: Exile Gideon, the Oathsworn and each creature your opponents control."));
    }
}
