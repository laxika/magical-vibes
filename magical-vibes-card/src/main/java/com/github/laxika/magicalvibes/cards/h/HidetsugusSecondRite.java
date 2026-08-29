package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerLifeTotalEquals;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "FDN", collectorNumber = "202")
@CardRegistration(set = "SOK", collectorNumber = "102")
public class HidetsugusSecondRite extends Card {

    public HidetsugusSecondRite() {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetPlayerLifeTotalEquals(10),
                new DealDamageToPlayersEffect(10, DamageRecipient.TARGET_PLAYER)));
    }
}
