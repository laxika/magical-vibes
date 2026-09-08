package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ActivePlayerHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "SOK", collectorNumber = "154")
public class EbonyOwlNetsuke extends Card {

    public EbonyOwlNetsuke() {
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new ConditionalEffect(
                new ActivePlayerHandAtLeast(7),
                new DealDamageToPlayersEffect(4, DamageRecipient.TARGET_PLAYER)));
    }
}
