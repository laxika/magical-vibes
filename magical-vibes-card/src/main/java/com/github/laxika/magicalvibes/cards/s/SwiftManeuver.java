package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;

@CardRegistration(set = "CSP", collectorNumber = "21")
public class SwiftManeuver extends Card {

    public SwiftManeuver() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.nextToTarget(2));
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
