package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedUntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TeferiHeroEmblemEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "207")
public class TeferiHeroOfDominaria extends Card {

    public TeferiHeroOfDominaria() {
        // +1: Draw a card. At the beginning of the next end step, untap up to two lands.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(1), new RegisterDelayedUntapPermanentsEffect(2, new PermanentIsLandPredicate())),
                "+1: Draw a card. At the beginning of the next end step, untap up to two lands."
        ));

        // −3: Put target nonland permanent into its owner's library third from the top.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new PutTargetPermanentIntoLibraryNFromTopEffect(2)),
                "\u22123: Put target nonland permanent into its owner's library third from the top.",
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        "Target must be a nonland permanent"
                )
        ));

        // −8: You get an emblem with "Whenever you draw a card, exile target permanent an opponent controls."
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new TeferiHeroEmblemEffect()),
                "\u22128: You get an emblem with \"Whenever you draw a card, exile target permanent an opponent controls.\""
        ));
    }
}
