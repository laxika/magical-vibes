package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "199")
@CardRegistration(set = "SIR", collectorNumber = "191")
@CardRegistration(set = "DSC", collectorNumber = "173")
@CardRegistration(set = "TDC", collectorNumber = "252")
@CardRegistration(set = "OTC", collectorNumber = "189")
public class CrawlingSensation extends Card {

    public CrawlingSensation() {
        // At the beginning of your upkeep, you may mill two cards.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(new MillEffect(2, MillRecipient.CONTROLLER), "Mill two cards?"));

        // Whenever one or more land cards are put into your graveyard from anywhere for the first
        // time each turn, create a 1/1 green Insect creature token.
        // "The first time one or more land cards are put into your graveyard from anywhere each turn".
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                OncePerTurnTriggerEffect.firstLandGraveyardEvent(new CreateTokenEffect("Insect", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.INSECT), Set.of(), Set.of())));
    }
}
