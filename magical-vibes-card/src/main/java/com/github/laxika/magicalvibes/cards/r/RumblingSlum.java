package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "GPT", collectorNumber = "126")
public class RumblingSlum extends Card {

    public RumblingSlum() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DealDamageToPlayersEffect(1, DamageRecipient.EACH_PLAYER));
    }
}
