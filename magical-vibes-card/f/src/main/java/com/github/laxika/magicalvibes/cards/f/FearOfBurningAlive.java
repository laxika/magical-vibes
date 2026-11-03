package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "135")
public class FearOfBurningAlive extends Card {

    public FearOfBurningAlive() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToPlayersEffect(4, DamageRecipient.EACH_OPPONENT));
        addTrigger(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(),
                TriggerOptions.DEFAULT.withInterveningIf(new Delirium()),
                new DealDamageToTargetCreatureDamagedPlayerControlsEffect(new EventValue()));
    }
}
