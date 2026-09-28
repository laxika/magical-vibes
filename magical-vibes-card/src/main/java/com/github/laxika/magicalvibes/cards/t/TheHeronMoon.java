package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EmrakulThePromisedEnd;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCardCopyAndCastWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileBottomCardOfTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "373")
@CardRegistration(set = "MB2", collectorNumber = "375")
@CardRegistration(set = "MB2", collectorNumber = "612")
public class TheHeronMoon extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds under collector number 373. */
    static {
        Card.registerOracle("TheHeronMoon", new OracleData(
                "The Heron Moon",
                CardType.LAND,
                Set.of(),
                null,
                null,
                List.of(),
                List.of(),
                Set.of(CardSupertype.LEGENDARY),
                List.of(),
                "{T}: Add {C}.\n{1}, {T}: Exile the bottom card of target opponent's library.\nWhenever one or more cards an opponent owns are put into exile, put a release counter on The Heron Moon. Then if it has thirteen or more release counters on it, sacrifice it and create a copy of Emrakul, the Promised End. Cast it without paying its mana cost.",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public TheHeronMoon() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {1}, {T}: Exile the bottom card of target opponent's library.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new ExileBottomCardOfTargetPlayerLibraryEffect()),
                "{1}, {T}: Exile the bottom card of target opponent's library.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));

        addEffect(EffectSlot.ON_OPPONENT_OWNED_CARD_EXILED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.RELEASE),
                new ConditionalEffect(
                        new SourceCounterThreshold(13, CounterType.RELEASE),
                        SequenceEffect.of(
                                new SacrificeSelfEffect(),
                                new CreateCardCopyAndCastWithoutPayingManaCostEffect(
                                        EmrakulThePromisedEnd::new)
                        )
                )
        ));
    }
}
