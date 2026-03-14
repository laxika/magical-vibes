package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ISD", collectorNumber = "2")
public class AngelOfFlightAlabaster extends Card {

    public AngelOfFlightAlabaster() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ReturnCardFromGraveyardEffect(GraveyardChoiceDestination.HAND, new CardSubtypePredicate(CardSubtype.SPIRIT)));
    }
}
