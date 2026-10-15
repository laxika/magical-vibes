package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "10")
public class TemplarKnight extends Card {

    public TemplarKnight() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(
                        new TapMultiplePermanentsCost(5, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsAttackingPredicate(),
                                new PermanentNamedPredicate("Templar Knight")
                        ))),
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                                        new CardTypePredicate(CardType.ARTIFACT)
                                )),
                                LibrarySearchDestination.BATTLEFIELD)
                ),
                "{W}, Tap five untapped attacking creatures you control named Templar Knight: Search your library for a legendary artifact card, put it onto the battlefield, then shuffle."
        ));
    }
}
