package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "180")
public class RedtoothVanguard extends Card {

    public RedtoothVanguard() {
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.YOU_CONTROL),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                UnlessPaysEffect.youMayPay("{2}", new PutCardsIntoHandEffect(Subjects.sourceCard()))
                        .withPrompt("Pay {2} to return Redtooth Vanguard from your graveyard to your hand?"));
    }
}
