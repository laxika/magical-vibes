package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "60")
@CardRegistration(set = "PIO", collectorNumber = "331")
public class OathOfJace extends Card {

    public OathOfJace() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(3),
                new DiscardEffect(2, DiscardRecipient.CONTROLLER));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ScryEffect(new PermanentCount(
                new PermanentIsPlaneswalkerPredicate(), CountScope.CONTROLLER)));
    }
}
