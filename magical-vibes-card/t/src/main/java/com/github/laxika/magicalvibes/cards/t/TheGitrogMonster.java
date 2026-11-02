package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "238")
@CardRegistration(set = "INR", collectorNumber = "431")
@CardRegistration(set = "SOI", collectorNumber = "245")
@CardRegistration(set = "SLD", collectorNumber = "1051")
@CardRegistration(set = "MB1", collectorNumber = "245")
@CardRegistration(set = "BLC", collectorNumber = "88")
@CardRegistration(set = "EOC", collectorNumber = "117")
public class TheGitrogMonster extends Card {

    public TheGitrogMonster() {
        // At the beginning of your upkeep, sacrifice The Gitrog Monster unless you sacrifice a land.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new SacrificeUnlessSacrificeOwnPermanentEffect(new PermanentIsLandPredicate(), "a land"));

        // You may play an additional land on each of your turns.
        addStaticEffect(new PlaysAdditionalLandEachTurnEffect(1));

        // Whenever one or more land cards are put into your graveyard from anywhere, draw a card.
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD,
                ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                new DrawCardEffect(1));
    }
}
