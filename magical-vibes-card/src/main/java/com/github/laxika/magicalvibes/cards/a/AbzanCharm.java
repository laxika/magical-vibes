package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "161")
public class AbzanCharm extends Card {

    public AbzanCharm() {
        var creatureFilter = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(3))),
                "Target must be a creature with power 3 or greater.");
        var targetCreatureFilter = new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature with power 3 or greater",
                        new ExileTargetPermanentEffect(),
                        creatureFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "You draw two cards and you lose 2 life",
                        List.of(new DrawCardEffect(2), new LoseLifeEffect(2, LoseLifeRecipient.CONTROLLER))),
                new ChooseOneEffect.ChooseOneOption(
                        "Distribute two +1/+1 counters among one or two target creatures",
                        List.of(DistributeCountersAmongTargetsEffect.evenlyAmongTargets(
                                CounterType.PLUS_ONE_PLUS_ONE, 2)),
                        targetCreatureFilter, null, 1, 2, false, null)
        )));
    }
}
