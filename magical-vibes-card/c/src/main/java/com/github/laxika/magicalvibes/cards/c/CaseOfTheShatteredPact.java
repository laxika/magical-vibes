package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "1")
public class CaseOfTheShatteredPact extends Card {

    public CaseOfTheShatteredPact() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(CardPredicateUtils.basicLand()));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new AllOf(List.of(
                        new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.WHITE))),
                        new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.BLUE))),
                        new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.BLACK))),
                        new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.RED))),
                        new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.GREEN))),
                        new NotCondition(new SourceIsSolved())
                )), new SolveSourceEffect()));

        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new ConditionalEffect(new AllOf(List.of(new SourceIsSolved())),
                                new GrantKeywordEffect(Set.of(
                                        Keyword.FLYING,
                                        Keyword.DOUBLE_STRIKE,
                                        Keyword.VIGILANCE
                                ), GrantScope.TARGET)));
    }
}
