package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllCreaturesUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.VenserEmblemEffect;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "135")
public class VenserTheSojourner extends Card {

    public VenserTheSojourner() {
        // +2: Exile target permanent you own. Return it to the battlefield under your control
        // at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new ExileTargetPermanentAndReturnAtEndStepEffect()),
                "+2: Exile target permanent you own. Return it to the battlefield under your control at the beginning of the next end step.",
                new OwnedPermanentPredicateTargetFilter(
                        new PermanentTruePredicate(),
                        "Target must be a permanent you own"
                )
        ));

        // −1: Creatures can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new MakeAllCreaturesUnblockableEffect()),
                "\u22121: Creatures can't be blocked this turn."
        ));

        // −8: You get an emblem with "Whenever you cast a spell, exile target permanent."
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new VenserEmblemEffect()),
                "\u22128: You get an emblem with \"Whenever you cast a spell, exile target permanent.\""
        ));
    }
}
