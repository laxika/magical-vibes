package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WAR", collectorNumber = "169")
public class NissaWhoShakesTheWorld extends Card {

    private static final String EMBLEM_TEXT = "Lands you control have indestructible.";

    public NissaWhoShakesTheWorld() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddManaWhenLandOfSubtypeTappedForManaEffect(CardSubtype.FOREST, ManaColor.GREEN, true));

        ControlledPermanentPredicateTargetFilter noncreatureLandYouControl =
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentNotPredicate(new PermanentIsCreaturePredicate()))),
                        "Target must be a noncreature land you control");
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3),
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new AnimatePermanentsEffect(
                                0, 0, List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.VIGILANCE, Keyword.HASTE), null, Set.of(),
                                GrantScope.TARGET, EffectDuration.PERMANENT)),
                "+1: Put three +1/+1 counters on up to one target noncreature land you control. "
                        + "Untap it. It becomes a 0/0 Elemental creature with vigilance and haste "
                        + "that's still a land.",
                null,
                +1,
                null,
                null,
                List.of(noncreatureLandYouControl),
                0,
                1
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(
                        new CreateEmblemEffect(
                                List.of(new GrantKeywordEffect(
                                        Keyword.INDESTRUCTIBLE, GrantScope.OWN_LANDS)),
                                EMBLEM_TEXT),
                        new SearchLibraryEffect(
                                new CardsInLibrary(CountScope.CONTROLLER),
                                new CardSubtypePredicate(CardSubtype.FOREST),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                "-8: You get an emblem with \"" + EMBLEM_TEXT + "\". Search your library for any "
                        + "number of Forest cards, put them onto the battlefield tapped, then shuffle."
        ));
    }
}
