package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "60")
public class TezzeretTheSeeker extends Card {

    public TezzeretTheSeeker() {
        // +1: Untap up to two target artifacts.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS)),
                "+1: Untap up to two target artifacts.",
                null, 1, null, null,
                List.of(
                        new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Target must be an artifact"),
                        new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Target must be an artifact")
                ),
                0, 2
        ));

        // −X: Search your library for an artifact card with mana value X or less, put it onto the battlefield, then shuffle.
        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(new SearchLibraryEffect(new CardTypePredicate(CardType.ARTIFACT),
                        LibrarySearchDestination.BATTLEFIELD, new ManaValueBound(false, 0))),
                "−X: Search your library for an artifact card with mana value X or less, put it onto the battlefield, then shuffle.",
                null
        ));

        // −5: Artifacts you control become artifact creatures with base power and toughness 5/5 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(new AnimatePermanentsEffect(new Fixed(5), new Fixed(5), List.of(), Set.of(), null, Set.of(),
                        GrantScope.OWN_PERMANENTS, EffectDuration.UNTIL_END_OF_TURN,
                        new PermanentIsArtifactPredicate())),
                "−5: Artifacts you control become artifact creatures with base power and toughness 5/5 until end of turn."
        ));
    }
}
