package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionSharedByOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.stream.Stream;

@CardRegistration(set = "RAV", collectorNumber = "8")
public class ConcertedEffort extends Card {

    private static final List<Keyword> SHARED_KEYWORDS = List.of(
            Keyword.FLYING,
            Keyword.FEAR,
            Keyword.FIRST_STRIKE,
            Keyword.DOUBLE_STRIKE,
            Keyword.FORESTWALK,
            Keyword.MOUNTAINWALK,
            Keyword.ISLANDWALK,
            Keyword.SWAMPWALK,
            Keyword.PLAINSWALK,
            Keyword.TRAMPLE,
            Keyword.VIGILANCE
    );

    public ConcertedEffort() {
        List<CardEffect> sharedEffects = Stream.concat(
                SHARED_KEYWORDS.stream().map(keyword -> (CardEffect) new ConditionalEffect(
                        new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasKeywordPredicate(keyword)
                        ))),
                        new GrantKeywordEffect(keyword, GrantScope.ALL_OWN_CREATURES)
                )),
                Stream.of(new GrantProtectionSharedByOwnCreaturesUntilEndOfTurnEffect())
        ).toList();
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new SequenceEffect(sharedEffects));
    }
}
