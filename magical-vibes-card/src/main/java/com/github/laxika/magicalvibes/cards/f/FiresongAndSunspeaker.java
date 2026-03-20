package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnSpellLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;

@CardRegistration(set = "DOM", collectorNumber = "280")
public class FiresongAndSunspeaker extends Card {

    public FiresongAndSunspeaker() {
        addEffect(EffectSlot.STATIC, new GrantLifelinkToControllerSpellsByColorEffect(CardColor.RED));
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new DealDamageOnSpellLifeGainEffect(3, CardColor.WHITE));
    }
}
