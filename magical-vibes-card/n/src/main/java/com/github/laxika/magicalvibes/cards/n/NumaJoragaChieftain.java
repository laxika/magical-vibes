package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaUpToNTimesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "KHC", collectorNumber = "71")
public class NumaJoragaChieftain extends Card {

    public NumaJoragaChieftain() {
        PermanentAllOfPredicate elfCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.ELF)));
        DistributeCountersAmongTargetsEffect distributeCounters =
                DistributeCountersAmongTargetsEffect.chosenAmongAnyNumberOfTargetCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE, new XValue(), elfCreature);

        // At the beginning of combat on your turn, you may pay {X}{X}. When you do, distribute
        // X +1/+1 counters among any number of target Elves.
        targetUpTo(new XValue(), new PermanentPredicateTargetFilter(
                elfCreature, "Target must be an Elf creature"), 100);
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new PayManaUpToNTimesEffect("{2}", Integer.MAX_VALUE, distributeCounters));
    }
}
