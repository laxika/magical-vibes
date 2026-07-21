package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.JaceUnravelerOfSecretsEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "70")
public class JaceUnravelerOfSecrets extends Card {

    public JaceUnravelerOfSecrets() {
        // +1: Scry 1, then draw a card.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new ScryEffect(1), new DrawCardEffect(1)),
                "+1: Scry 1, then draw a card."
        ));

        // −2: Return target creature to its owner's hand.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(ReturnToHandEffect.target()),
                "\u22122: Return target creature to its owner's hand.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        // −8: You get an emblem with "Whenever an opponent casts their first spell each turn, counter that spell."
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new JaceUnravelerOfSecretsEmblemEffect()),
                "\u22128: You get an emblem with \"Whenever an opponent casts their first spell each turn, counter that spell.\""
        ));
    }
}
