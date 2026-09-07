package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ONS", collectorNumber = "136")
public class DeathMatch extends Card {

    public DeathMatch() {
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new MayEffect(
                        new BoostTargetCreatureEffect(-3, -3),
                        "Have target creature get -3/-3 until end of turn?"
                ));
    }
}
