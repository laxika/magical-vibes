package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "32")
public class AncestralKnowledge extends Card {

    public AncestralKnowledge() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{1}"));

        // No player target: the look falls back to the controller's own library.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new LookAtTopCardsOfTargetLibraryEffect(10, TargetLibraryAction.MAY_EXILE_ANY_NUMBER));

        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new ShuffleLibraryEffect(false));
    }
}
