package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "298")
public class VivienNaturesAvenger extends Card {

    public VivienNaturesAvenger() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3)),
                "+1: Put three +1/+1 counters on up to one target creature.",
                TargetFilters.creature(),
                +1, null, null,
                List.of(), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                -1,
                List.of(new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardTypePredicate(CardType.CREATURE), LibrarySearchDestination.HAND)),
                "−1: Reveal cards from the top of your library until you reveal a creature card. Put that card into your hand and the rest on the bottom of your library in a random order."
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(
                        new BoostTargetCreatureEffect(10, 10),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)
                ),
                "−6: Target creature gets +10/+10 and gains trample until end of turn.",
                TargetFilters.creature()
        ));
    }
}
