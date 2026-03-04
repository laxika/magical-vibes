package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrainLifePerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "97")
public class TezzeretAgentOfBolas extends Card {

    public TezzeretAgentOfBolas() {
        // +1: Look at the top five cards of your library. You may reveal an artifact card
        // from among them and put it into your hand. Put the rest on the bottom of your library in any order.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new LookAtTopCardsMayRevealCreaturePutIntoHandRestOnBottomEffect(5, Set.of(CardType.ARTIFACT))),
                "+1: Look at the top five cards of your library. You may reveal an artifact card from among them and put it into your hand. Put the rest on the bottom of your library in any order."
        ));

        // −1: Target artifact becomes an artifact creature with base power and toughness 5/5.
        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new AnimateTargetPermanentEffect(5, 5)),
                "\u22121: Target artifact becomes an artifact creature with base power and toughness 5/5.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Target must be an artifact"
                )
        ));

        // −4: Target player loses X life and you gain X life, where X is twice the number of artifacts you control.
        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new DrainLifePerControlledPermanentEffect(new PermanentIsArtifactPredicate(), 2)),
                "\u22124: Target player loses X life and you gain X life, where X is twice the number of artifacts you control."
        ));
    }
}
