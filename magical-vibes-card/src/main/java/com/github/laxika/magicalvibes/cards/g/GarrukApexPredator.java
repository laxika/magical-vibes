package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.BoostAttackingCreatureOnAttacksYouEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemRecipient;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "210")
public class GarrukApexPredator extends Card {

    public GarrukApexPredator() {
        // +1: Destroy another target planeswalker.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DestroyTargetPermanentEffect()),
                "+1: Destroy another target planeswalker.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsPlaneswalkerPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another planeswalker"
                )
        ));

        // +1: Create a 3/3 black Beast creature token with deathtouch.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect("Beast", 3, 3, CardColor.BLACK,
                        List.of(CardSubtype.BEAST), Set.of(Keyword.DEATHTOUCH), Set.of())),
                "+1: Create a 3/3 black Beast creature token with deathtouch."
        ));

        // −3: Destroy target creature. You gain life equal to its toughness.
        // Life gain is listed first so TargetToughness reads the creature while it is still on the
        // battlefield — the same ordering Condemn uses.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new GainLifeEffect(new TargetToughness()), new DestroyTargetPermanentEffect()),
                "−3: Destroy target creature. You gain life equal to its toughness.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));

        // −8: Target opponent gets an emblem with "Whenever a creature attacks you, it gets +5/+5
        // and gains trample until end of turn."
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateEmblemEffect(
                        List.of(new BoostAttackingCreatureOnAttacksYouEffect(5, 5, Set.of(Keyword.TRAMPLE))),
                        "Whenever a creature attacks you, it gets +5/+5 and gains trample until end of turn.",
                        EmblemRecipient.TARGET_PLAYER)),
                "−8: Target opponent gets an emblem with \"Whenever a creature attacks you, it "
                        + "gets +5/+5 and gains trample until end of turn.\"",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
