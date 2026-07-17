package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "195")
public class SigilBlessing extends Card {

    public SigilBlessing() {
        // Until end of turn, target creature you control gets +3/+3...
        // Modeled as an extra +2/+2 on the target, stacked on top of the +1/+1 below,
        // so the target nets +3/+3 while other creatures net +1/+1.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                "Target must be a creature you control"
        )).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));

        // ...and other creatures you control get +1/+1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));
    }
}
