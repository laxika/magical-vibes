package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.MinimumMatchingAttackers;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ValorsReachTagTeam extends Card {

    public ValorsReachTagTeam() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, 2, "Warrior", 3, 2, null,
                Set.of(CardColor.RED, CardColor.WHITE), List.of(CardSubtype.WARRIOR), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new ConditionalEffect(
                                new AllConditions(List.of(
                                        new SourceIsAttacking(),
                                        new MinimumMatchingAttackers(2, new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentIsTokenPredicate()))))),
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))),
                List.of(), false, false, false, 0, Set.of()));
    }
}
