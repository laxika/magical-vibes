package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "13")
public class EldraziDisplacer extends Card {

    public EldraziDisplacer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{C}",
                List.of(new FlickerEffect(
                        FlickerScope.TARGET,
                        null,
                        ReturnTiming.IMMEDIATE,
                        TurnStep.END_STEP,
                        true,
                        null,
                        null,
                        0,
                        false,
                        false)),
                "{2}{C}: Exile another target creature, then return it to the battlefield tapped under its owner's control.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another creature"
                )
        ));
    }
}
