package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "MKM", collectorNumber = "86")
@CardRegistration(set = "MKM", collectorNumber = "343")
public class HomicideInvestigator extends Card {

    public HomicideInvestigator() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES,
                new OncePerTurnTriggerEffect(CreateTokenEffect.ofClueToken(1)));
    }
}
