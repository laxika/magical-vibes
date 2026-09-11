package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeEffect;

@CardRegistration(set = "SCG", collectorNumber = "106")
@CardRegistration(set = "VMA", collectorNumber = "190")
public class SulfuricVortex extends Card {

    public SulfuricVortex() {
        addEffect(EffectSlot.STATIC, new PlayersCantGainLifeEffect());
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(2, DamageRecipient.EACH_PLAYER));
    }
}
