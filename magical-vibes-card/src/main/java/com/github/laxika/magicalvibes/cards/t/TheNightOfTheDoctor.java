package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "24")
public class TheNightOfTheDoctor extends Card {

    public TheNightOfTheDoctor() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));

        addEffect(EffectSlot.SAGA_CHAPTER_II, SequenceEffect.of(
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSupertypePredicate(CardSupertype.LEGENDARY))))
                        .targetGraveyard(true)
                        .build(),
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a first strike counter on it",
                                new PutCounterOnReferencedPermanentEffect(
                                        PermanentReference.RETURNED, CounterType.FIRST_STRIKE)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a vigilance counter on it",
                                new PutCounterOnReferencedPermanentEffect(
                                        PermanentReference.RETURNED, CounterType.VIGILANCE)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a lifelink counter on it",
                                new PutCounterOnReferencedPermanentEffect(
                                        PermanentReference.RETURNED, CounterType.LIFELINK))
                ))
        ));
    }
}
