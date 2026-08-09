package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDefendingPlayerCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "BOK", collectorNumber = "116")
public class RoninCliffrider extends Card {

    public RoninCliffrider() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(1));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new DealDamageToDefendingPlayerCreaturesEffect(1),
                "Have Ronin Cliffrider deal 1 damage to each creature defending player controls?"));
    }
}
