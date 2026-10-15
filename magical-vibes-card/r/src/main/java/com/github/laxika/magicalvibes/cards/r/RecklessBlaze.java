package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedDamagedCreatureDeathTriggerEffect;

@CardRegistration(set = "TLE", collectorNumber = "124")
@CardRegistration(set = "TLE", collectorNumber = "197")
public class RecklessBlaze extends Card {

    public RecklessBlaze() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(5));
        addEffect(EffectSlot.SPELL, new RegisterDelayedDamagedCreatureDeathTriggerEffect(
                new AwardManaEffect(ManaColor.RED)));
    }
}
