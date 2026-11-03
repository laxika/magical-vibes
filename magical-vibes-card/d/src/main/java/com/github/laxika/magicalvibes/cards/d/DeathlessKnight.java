package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "208")
public class DeathlessKnight extends Card {

    public DeathlessKnight() {
        addTrigger(new TriggerCondition(Triggers.youGainLife().firstTimeEachTurn(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
