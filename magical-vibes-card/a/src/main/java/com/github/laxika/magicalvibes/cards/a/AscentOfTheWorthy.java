package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageToChosenCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "202")
public class AscentOfTheWorthy extends Card {

    public AscentOfTheWorthy() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new RedirectAllDamageToChosenCreatureUntilNextTurnEffect());

        addEffect(EffectSlot.SAGA_CHAPTER_II, new RedirectAllDamageToChosenCreatureUntilNextTurnEffect());

        addEffect(EffectSlot.SAGA_CHAPTER_III, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .enterWithCounter(CounterType.FLYING)
                .enterWithCounterCount(1)
                .grantSubtypes(List.of(CardSubtype.ANGEL, CardSubtype.WARRIOR))
                .build());
    }
}
