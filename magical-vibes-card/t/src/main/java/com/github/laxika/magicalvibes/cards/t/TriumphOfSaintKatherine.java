package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardAndShuffleTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "17")
public class TriumphOfSaintKatherine extends Card {

    public TriumphOfSaintKatherine() {
        // "Is put into your graveyard from the battlefield": a card goes to its owner's graveyard.
        addTrigger(Triggers.dies(new PermanentOwnedBySourceControllerPredicate(), ObjectRelation.THIS),
                new ExileSourceCardFromGraveyardAndShuffleTopCardsEffect(6));
        addCastingOption(new MiracleCast("{1}{W}"));
    }
}
