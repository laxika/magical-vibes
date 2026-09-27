package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SecretCouncilEffect;

@CardRegistration(set = "LTC", collectorNumber = "25")
@CardRegistration(set = "LTC", collectorNumber = "108")
public class TrapTheTrespassers extends Card {

    public TrapTheTrespassers() {
        addEffect(EffectSlot.SPELL, new SecretCouncilEffect());
    }
}
