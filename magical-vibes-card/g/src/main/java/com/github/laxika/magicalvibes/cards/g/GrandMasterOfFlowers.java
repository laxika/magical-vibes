package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "17")
public class GrandMasterOfFlowers extends Card {

    private static final SourceCounterThreshold SEVEN_LOYALTY =
            new SourceCounterThreshold(7, CounterType.LOYALTY);

    private static final PermanentPredicateTargetFilter CREATURE_WITHOUT_COMBAT_KEYWORDS =
            new PermanentPredicateTargetFilter(
                    new PermanentAllOfPredicate(List.of(
                            new PermanentIsCreaturePredicate(),
                            new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FIRST_STRIKE)),
                            new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.DOUBLE_STRIKE)),
                            new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.VIGILANCE))
                    )),
                    "Target must be a creature without first strike, double strike, or vigilance");

    public GrandMasterOfFlowers() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(SEVEN_LOYALTY,
                new AnimatePermanentsEffect(7, 7, List.of(CardSubtype.DRAGON, CardSubtype.GOD),
                        Set.of(Keyword.FLYING, Keyword.INDESTRUCTIBLE))));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(SEVEN_LOYALTY,
                new SetCardTypesEffect(Set.of(CardType.CREATURE), GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new LockTargetPermanentEffect(true, true, false, EffectDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Target creature without first strike, double strike, or vigilance can't attack or block until your next turn.",
                CREATURE_WITHOUT_COMBAT_KEYWORDS));
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new SearchLibraryAndOrGraveyardForCardToHandEffect(
                        new CardNamedPredicate("Monk of the Open Hand"))),
                "+1: Search your library and/or graveyard for a card named Monk of the Open Hand, reveal it, and put it into your hand. If you search your library this way, shuffle."));
    }
}
