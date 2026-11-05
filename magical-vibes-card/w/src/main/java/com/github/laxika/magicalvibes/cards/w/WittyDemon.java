package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.StartingDeckAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB1", collectorNumber = "50")
public class WittyDemon extends Card {

    public WittyDemon() {
        StartingDeckAtLeast startingDeckCondition = new StartingDeckAtLeast(13);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ConditionalEffect.unless(startingDeckCondition, new SearchLibraryEffect()),
                ConditionalEffect.unless(new NotCondition(startingDeckCondition),
                        new DealDamageToPlayersEffect(4, DamageRecipient.CONTROLLER)));
    }
}
