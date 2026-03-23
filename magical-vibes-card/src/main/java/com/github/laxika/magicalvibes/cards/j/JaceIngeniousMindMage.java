package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "280")
public class JaceIngeniousMindMage extends Card {

    public JaceIngeniousMindMage() {
        // +1: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(1)),
                "+1: Draw a card."
        ));

        // +1: Untap all creatures you control.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new UntapAllControlledPermanentsEffect(new PermanentIsCreaturePredicate())),
                "+1: Untap all creatures you control."
        ));

        // −9: Gain control of up to three target creatures.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new GainControlOfTargetPermanentEffect()),
                "\u22129: Gain control of up to three target creatures.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                ),
                -9, null, null,
                List.of(), 0, 3
        ));
    }
}
