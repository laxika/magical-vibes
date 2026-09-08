package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "13")
public class ElspethConquersDeath extends Card {

    public ElspethConquersDeath() {
        PermanentPredicate chapterOneFilter = new PermanentAllOfPredicate(List.of(
                new PermanentMinManaValuePredicate(3),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ExileTargetPermanentEffect(chapterOneFilter));
        setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(
                new PermanentPredicateTargetFilter(
                        chapterOneFilter,
                        "Target must be a permanent an opponent controls with mana value 3 or greater")));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new IncreaseSpellCostUntilNextTurnEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                2));

        addEffect(EffectSlot.SAGA_CHAPTER_III, SequenceEffect.of(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.PLANESWALKER))))
                        .targetGraveyard(true)
                        .build(),
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a +1/+1 counter on it",
                                new PutCounterOnReferencedPermanentEffect(
                                        PermanentReference.RETURNED,
                                        CounterType.PLUS_ONE_PLUS_ONE)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a loyalty counter on it",
                                new PutCounterOnReferencedPermanentEffect(
                                        PermanentReference.RETURNED,
                                        CounterType.LOYALTY))
                ))
        ));
    }
}
