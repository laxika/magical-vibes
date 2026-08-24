package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeDayAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "MID", collectorNumber = "130")
public class BrimstoneVandal extends Card {

    public BrimstoneVandal() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeDayAsEntersEffect());
        addEffect(EffectSlot.ON_DAY_NIGHT_CHANGE,
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
    }
}
