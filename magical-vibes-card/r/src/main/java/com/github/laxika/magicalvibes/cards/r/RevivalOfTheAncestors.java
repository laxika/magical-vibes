package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "218")
public class RevivalOfTheAncestors extends Card {

    public RevivalOfTheAncestors() {
        // Chapter I: three 1/1 white Spirit tokens, without flying (unlike CreateTokenEffect.whiteSpirit).
        addTrigger(Triggers.chapter(1), new CreateTokenEffect(CardType.CREATURE, 3, "Spirit", 1, 1,
                CardColor.WHITE, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of(), false, false,
                List.of(), List.of(), false, false, false, 0, Set.of()));
        addTrigger(Triggers.chapter(2),
                DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3), new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate()))));
        setSagaChapterTargetFilter(2, Set.of(TargetFilters.creatureYouControl()));
        addTrigger(Triggers.chapter(3), new GrantKeywordEffect(
                Set.of(Keyword.TRAMPLE, Keyword.LIFELINK), GrantScope.ALL_OWN_CREATURES));
    }
}
