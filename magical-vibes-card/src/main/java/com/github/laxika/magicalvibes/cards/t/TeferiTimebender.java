package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "270")
public class TeferiTimebender extends Card {

    public TeferiTimebender() {
        // +2: Untap up to one target artifact or creature.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new UntapTargetPermanentEffect()),
                "+2: Untap up to one target artifact or creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        "Target must be an artifact or creature"
                ),
                2, null, null,
                List.of(), 0, 1
        ));

        // −3: You gain 2 life and draw two cards.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new GainLifeEffect(2), new DrawCardEffect(2)),
                "\u22123: You gain 2 life and draw two cards."
        ));

        // −9: Take an extra turn after this one.
        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(new ControllerExtraTurnEffect(1)),
                "\u22129: Take an extra turn after this one."
        ));
    }
}
