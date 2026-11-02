package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "180")
@CardRegistration(set = "GN3", collectorNumber = "94")
@CardRegistration(set = "2X2", collectorNumber = "144")
@CardRegistration(set = "M3C", collectorNumber = "226")
@CardRegistration(set = "OTC", collectorNumber = "191")
@CardRegistration(set = "KHC", collectorNumber = "60")
@CardRegistration(set = "ZNC", collectorNumber = "63")
public class ElvishRejuvenator extends Card {

    public ElvishRejuvenator() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                LookAtTopCardsEffect.mayPutOneMatchingOntoBattlefieldRestOnBottomRandom(
                        5, new CardTypePredicate(CardType.LAND)));
    }
}
