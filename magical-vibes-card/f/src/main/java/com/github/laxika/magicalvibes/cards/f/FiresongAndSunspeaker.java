package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnSpellLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "280")
@CardRegistration(set = "2X2", collectorNumber = "215")
@CardRegistration(set = "MUL", collectorNumber = "39")
@CardRegistration(set = "MUL", collectorNumber = "104")
@CardRegistration(set = "MUL", collectorNumber = "169")
public class FiresongAndSunspeaker extends Card {

    public FiresongAndSunspeaker() {
        addStaticEffect(new GrantLifelinkToControllerSpellsByColorEffect(CardColor.RED));
        addTrigger(Triggers.youGainLife(), new DealDamageOnSpellLifeGainEffect(3, CardColor.WHITE));
    }
}
