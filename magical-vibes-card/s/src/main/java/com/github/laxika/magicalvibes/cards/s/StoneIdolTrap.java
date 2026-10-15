package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCreatedPermanentsAtControllerEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "93")
public class StoneIdolTrap extends Card {

    public StoneIdolTrap() {
        // This spell costs {1} less to cast for each attacking creature.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate())),
                CountScope.ANY_PLAYER)));

        // Create a 6/12 colorless Construct artifact creature token with trample.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, 1, "Construct", 6, 12,
                null, null, List.of(CardSubtype.CONSTRUCT), Set.of(Keyword.TRAMPLE),
                Set.of(CardType.ARTIFACT), false, false, Map.of(), List.of(),
                false, false, false, 0, Set.of()));
        addEffect(EffectSlot.SPELL, new ExileCreatedPermanentsAtControllerEndStepEffect());
    }
}
