package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "218")
public class RevivalOfTheAncestors extends Card {

    public RevivalOfTheAncestors() {
        // Chapter I: three 1/1 white Spirit tokens, without flying (unlike CreateTokenEffect.whiteSpirit).
        addEffect(EffectSlot.SAGA_CHAPTER_I, new CreateTokenEffect(CardType.CREATURE, 3, "Spirit", 1, 1,
                CardColor.WHITE, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of(), false, false,
                Map.of(), List.of(), false, false, false, 0, Set.of()));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3), new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate()))));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(TargetFilters.creatureYouControl()));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new GrantKeywordEffect(
                Set.of(Keyword.TRAMPLE, Keyword.LIFELINK), GrantScope.ALL_OWN_CREATURES));
    }
}
