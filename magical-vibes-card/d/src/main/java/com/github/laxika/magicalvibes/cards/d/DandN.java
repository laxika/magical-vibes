package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "79")
@CardRegistration(set = "CHR", collectorNumber = "18")
@CardRegistration(set = "TSB", collectorNumber = "19")
@CardRegistration(set = "SLD", collectorNumber = "2138")
@CardRegistration(set = "SLD", collectorNumber = "2139")
@CardRegistration(set = "ARN", collectorNumber = "12")
public class DandN extends Card {

    public DandN() {
        // "This creature can't attack unless defending player controls an Island."
        addStaticEffect(new CantAttackUnlessEffect(
                new DefendingPlayerControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                "an Island"
        ));

        // "When you control no Islands, sacrifice this creature." —
        // State-triggered ability (MTG rule 603.8).
        addTrigger(Triggers.battlefieldHasAtMost(0, new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ISLAND)))),
                new SacrificeSelfEffect());
    }
}
