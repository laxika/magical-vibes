package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "BRC", collectorNumber = "18")
@CardRegistration(set = "BRC", collectorNumber = "65")
public class SmeltingVat extends Card {

    public SmeltingVat() {
        CardAllOfPredicate noncreatureArtifact = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsArtifactPredicate(), "another artifact", true,
                                false, true, false),
                        new LookAtTopCardsEffect(
                                new Fixed(8), new Fixed(2), noncreatureArtifact,
                                LookDestination.BOTTOM_OF_LIBRARY_RANDOM, false,
                                LibrarySearchDestination.BATTLEFIELD, true, false, null,
                                null, false, 0, false, false, false, false, 0, null,
                                false, null, new XValue())
                ),
                "{1}, {T}, Sacrifice another artifact: Reveal the top eight cards of your library. "
                        + "Put up to two noncreature artifact cards with total mana value less than "
                        + "or equal to the sacrificed artifact's mana value from among them onto the "
                        + "battlefield and the rest on the bottom of your library in a random order."
        ));
    }
}
