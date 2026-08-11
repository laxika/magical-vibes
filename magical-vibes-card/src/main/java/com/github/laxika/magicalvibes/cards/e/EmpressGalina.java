package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "54")
public class EmpressGalina extends Card {

    public EmpressGalina() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{U}",
                List.of(new GainControlOfTargetEffect(ControlDuration.PERMANENT)),
                "{U}{U}, {T}: Gain control of target legendary permanent.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                        "Target must be a legendary permanent"
                )
        ));
    }
}
