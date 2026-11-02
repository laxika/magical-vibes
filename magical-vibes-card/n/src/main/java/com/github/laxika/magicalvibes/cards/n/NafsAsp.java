package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterLoseLifeAtNextDrawStepUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "264")
@CardRegistration(set = "ARN", collectorNumber = "52")
public class NafsAsp extends Card {

    public NafsAsp() {
        // Whenever this creature deals damage to a player, that player loses 1 life at the beginning
        // of their next draw step unless they pay {1} before that draw step. The damaged player is
        // carried as the trigger's (non-targeting) target; a delayed pay-or-lose-life obligation
        // fires at that player's next draw step.
        addTrigger(Triggers.dealsDamageToPlayer(),
                new RegisterLoseLifeAtNextDrawStepUnlessPaysEffect(1, 1));
    }
}
