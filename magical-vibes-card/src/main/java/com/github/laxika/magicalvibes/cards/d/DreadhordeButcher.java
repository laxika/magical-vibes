package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "WAR", collectorNumber = "194")
public class DreadhordeButcher extends Card {

    public DreadhordeButcher() {
        addEffect(EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
                new PutCountersOnSourceEffect(1, 1, 1));
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(new SourcePower()));
    }
}
