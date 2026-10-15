package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsDistinctPermanentNamesCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "233")
public class MonumentToPerfection extends Card {

    private static final CardPredicate SEARCHABLE_LAND = new CardAllOfPredicate(List.of(
            new CardTypePredicate(CardType.LAND),
            new CardAnyOfPredicate(List.of(
                    new CardSupertypePredicate(CardSupertype.BASIC),
                    new CardSubtypePredicate(CardSubtype.SPHERE),
                    new CardSubtypePredicate(CardSubtype.LOCUS)
            ))
    ));

    private static final PermanentPredicate ELIGIBLE_CONTROLLED_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentAnyOfPredicate(List.of(
                    new PermanentHasSupertypePredicate(CardSupertype.BASIC),
                    new PermanentHasSubtypePredicate(CardSubtype.SPHERE),
                    new PermanentHasSubtypePredicate(CardSubtype.LOCUS)
            ))
    ));

    public MonumentToPerfection() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new SearchLibraryEffect(SEARCHABLE_LAND, LibrarySearchDestination.HAND)),
                "{3}, {T}: Search your library for a basic, Sphere, or Locus land card, reveal it, put it into your hand, then shuffle."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new LosesAllAbilitiesEffect(GrantScope.SELF, EffectDuration.UNTIL_END_OF_TURN),
                        new AnimatePermanentsEffect(
                                9,
                                9,
                                List.of(CardSubtype.PHYREXIAN, CardSubtype.CONSTRUCT),
                                Set.of(),
                                null,
                                Set.of(CardType.ARTIFACT)
                        ),
                        new GrantKeywordEffect(Set.of(Keyword.INDESTRUCTIBLE, Keyword.TOXIC), GrantScope.SELF)
                ),
                "{3}: This artifact becomes a 9/9 Phyrexian Construct artifact creature, loses all abilities, and gains indestructible and toxic 9. Activate only if there are nine or more lands with different names among the basic, Sphere, and Locus lands you control."
        ).withActivationCondition(
                new ControlsDistinctPermanentNamesCount(9, ELIGIBLE_CONTROLLED_LAND),
                "Activate only if there are nine or more lands with different names among the basic, Sphere, and Locus lands you control."
        ));
    }
}
