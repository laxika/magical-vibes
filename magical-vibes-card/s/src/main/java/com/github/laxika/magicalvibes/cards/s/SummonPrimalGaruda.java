package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "37")
@CardRegistration(set = "FIN", collectorNumber = "360")
public class SummonPrimalGaruda extends Card {

    public SummonPrimalGaruda() {
        var tappedCreatureOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate(),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        var anotherCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new DealDamageToTargetCreatureEffect(4, tappedCreatureOpponentControls));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(
                new PermanentPredicateTargetFilter(
                        tappedCreatureOpponentControls,
                        "Target must be a tapped creature an opponent controls")
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new BoostTargetCreatureEffect(1, 0, anotherCreatureYouControl));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET, anotherCreatureYouControl));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_II, Set.of(
                new PermanentPredicateTargetFilter(
                        anotherCreatureYouControl,
                        "Target must be another creature you control")
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new BoostTargetCreatureEffect(1, 0, anotherCreatureYouControl));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET, anotherCreatureYouControl));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_III, Set.of(
                new PermanentPredicateTargetFilter(
                        anotherCreatureYouControl,
                        "Target must be another creature you control")
        ));
    }
}
