package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "310")
public class PufferExtract extends Card {

    public PufferExtract() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(
                        new BoostTargetCreatureEffect(new XValue(), new XValue()),
                        new DestroyTargetPermanentAtEndStepEffect()
                ),
                "{X}, {T}: Target creature you control gets +X/+X until end of turn. Destroy it at the beginning of the next end step.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )
        ));
    }
}
