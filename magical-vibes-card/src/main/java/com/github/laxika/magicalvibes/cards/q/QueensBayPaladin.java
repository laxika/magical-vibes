package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "115")
@CardRegistration(set = "LCI", collectorNumber = "368")
public class QueensBayPaladin extends Card {

    public QueensBayPaladin() {
        ReturnCardFromGraveyardEffect returnVampire = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardSubtypePredicate(CardSubtype.VAMPIRE))
                .targetGraveyard(true)
                .upTo(true)
                .enterWithCounter(CounterType.FINALITY)
                .enterWithCounterCount(1)
                .loseLifeEqualToManaValue(true)
                .build();

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, returnVampire);
        addEffect(EffectSlot.ON_ATTACK, returnVampire);
    }
}
