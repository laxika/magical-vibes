package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "MB2", collectorNumber = "334")
@CardRegistration(set = "MB2", collectorNumber = "571")
public class ZoneOfFlame extends Card {

    public ZoneOfFlame() {
        setEnchantZone(true);

        DealDamageToPlayersEffect trigger =
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT);
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD, trigger);
        addEffect(EffectSlot.ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD, trigger);
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, trigger);
    }
}
