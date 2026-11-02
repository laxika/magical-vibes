package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "222")
@CardRegistration(set = "LCI", collectorNumber = "229")
@CardRegistration(set = "LCI", collectorNumber = "330")
@CardRegistration(set = "SLD", collectorNumber = "320")
public class GishathSunsAvatar extends Card {

    public GishathSunsAvatar() {
        // Vigilance, trample, haste are auto-loaded from Scryfall
        // Whenever Gishath, Sun's Avatar deals combat damage to a player, reveal that many cards
        // from the top of your library. Put any number of Dinosaur creature cards from among them
        // onto the battlefield and the rest on the bottom of your library in a random order.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new LookAtTopXCardsPermanentsToBattlefieldRestToGraveyardEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardSubtypePredicate(CardSubtype.DINOSAUR)
                        )),
                        null,
                        true
                ));
    }
}
