package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToTargetsThenReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SorinLordOfInnistradEmblemEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "142")
public class SorinLordOfInnistrad extends Card {

    public SorinLordOfInnistrad() {
        // +1: Create a 1/1 black Vampire creature token with lifelink.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect("Vampire", 1, 1, CardColor.BLACK,
                        List.of(CardSubtype.VAMPIRE), Set.of(Keyword.LIFELINK), Set.of())),
                "+1: Create a 1/1 black Vampire creature token with lifelink."
        ));

        // −2: You get an emblem with "Creatures you control get +1/+0."
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new SorinLordOfInnistradEmblemEffect()),
                "\u22122: You get an emblem with \"Creatures you control get +1/+0.\"."
        ));

        // −6: Destroy up to three target creatures and/or other planeswalkers.
        // Return each card put into a graveyard this way to the battlefield under your control.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new DestroyUpToTargetsThenReturnFromGraveyardEffect()),
                "\u22126: Destroy up to three target creatures and/or other planeswalkers. Return each card put into a graveyard this way to the battlefield under your control.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsPlaneswalkerPredicate(),
                                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                                ))
                        )),
                        "Target must be a creature or another planeswalker"
                ),
                -6, null, null,
                List.of(), 0, 3
        ));
    }
}
