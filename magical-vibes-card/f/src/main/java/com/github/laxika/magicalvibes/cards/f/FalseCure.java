package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "146")
public class FalseCure extends Card {

    public FalseCure() {
        addSpellEffect(new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                new TriggeredAbility(Triggers.playerGainsLife(),
                        new LoseLifeEffect(new Scaled(new EventValue(), 2), LoseLifeRecipient.TRIGGERING_PLAYER))));
    }
}
