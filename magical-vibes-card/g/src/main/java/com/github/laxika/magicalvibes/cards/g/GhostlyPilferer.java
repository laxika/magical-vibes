package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "52")
@CardRegistration(set = "M21", collectorNumber = "350")
@CardRegistration(set = "NCC", collectorNumber = "223")
@CardRegistration(set = "OTC", collectorNumber = "98")
@CardRegistration(set = "VOC", collectorNumber = "105")
public class GhostlyPilferer extends Card {

    public GhostlyPilferer() {
        addTrigger(Triggers.becomesUntapped(),
                new MayPayManaEffect("{2}", new DrawCardEffect(), "Pay {2} to draw a card?"));

        addTrigger(Triggers.opponentCastsSpell()
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryCastFromZonePredicate(Zone.HAND))),
                new DrawCardEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null), new MakeCreatureUnblockableEffect(true)),
                "Discard a card: This creature can't be blocked this turn."));
    }
}
