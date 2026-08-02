package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "187")
public class NissaWorldwaker extends Card {

    public NissaWorldwaker() {
        // +1: Target land you control becomes a 4/4 Elemental creature with trample. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new AnimatePermanentsEffect(4, 4, List.of(CardSubtype.ELEMENTAL),
                        Set.of(Keyword.TRAMPLE), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.PERMANENT)),
                "+1: Target land you control becomes a 4/4 Elemental creature with trample. It's still a land.",
                TargetFilters.landYouControl()
        ));

        // +1: Untap up to four target Forests.
        PermanentPredicateTargetFilter forest = new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST), "Target must be a Forest");
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS)),
                "+1: Untap up to four target Forests.",
                null, +1, null, null,
                List.<TargetFilter>of(forest, forest, forest, forest),
                0, 4
        ));

        // −7: Search your library for any number of basic land cards, put them onto the battlefield,
        //     then shuffle. Those lands become 4/4 Elemental creatures with trample. They're still lands.
        // "Any number" is bounded by the library size; the restricted search may stop early.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new SearchLibraryEffect(
                        new CardsInLibrary(CountScope.CONTROLLER),
                        new CardAllOfPredicate(List.of(
                                new CardSupertypePredicate(CardSupertype.BASIC),
                                new CardTypePredicate(CardType.LAND))),
                        new AnimatePermanentsEffect(4, 4, List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.TRAMPLE), null, Set.of(),
                                GrantScope.OWN_PERMANENTS, EffectDuration.PERMANENT))),
                "−7: Search your library for any number of basic land cards, put them onto the "
                        + "battlefield, then shuffle. Those lands become 4/4 Elemental creatures "
                        + "with trample. They're still lands."
        ));
    }
}
