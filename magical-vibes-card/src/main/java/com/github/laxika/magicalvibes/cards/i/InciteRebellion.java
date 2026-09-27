package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffect;

@CardRegistration(set = "C14", collectorNumber = "37")
public class InciteRebellion extends Card {

    public InciteRebellion() {
        addEffect(EffectSlot.SPELL,
                new DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffect());
    }
}
