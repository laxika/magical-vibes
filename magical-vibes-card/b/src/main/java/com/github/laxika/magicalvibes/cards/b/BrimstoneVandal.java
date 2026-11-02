package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "130")
@CardRegistration(set = "DBL", collectorNumber = "130")
public class BrimstoneVandal extends Card {

    public BrimstoneVandal() {
        addStaticEffect(new BecomeDayAsEntersEffect());
        addTrigger(Triggers.dayBecomesNightOrNightBecomesDay(),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
    }
}
