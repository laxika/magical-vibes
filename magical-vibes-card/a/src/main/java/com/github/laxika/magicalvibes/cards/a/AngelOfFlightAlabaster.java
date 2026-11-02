package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "2")
@CardRegistration(set = "SIS", collectorNumber = "1")
@CardRegistration(set = "VOC", collectorNumber = "77")
public class AngelOfFlightAlabaster extends Card {

    public AngelOfFlightAlabaster() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                ReturnCardFromGraveyardEffect.builder().destination(GraveyardChoiceDestination.HAND).filter(new CardSubtypePredicate(CardSubtype.SPIRIT)).targetGraveyard(true).mandatory(true).build());
    }
}
