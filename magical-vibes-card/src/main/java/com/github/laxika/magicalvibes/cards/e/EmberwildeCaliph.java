package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;

@CardRegistration(set = "MIR", collectorNumber = "262")
public class EmberwildeCaliph extends Card {

    public EmberwildeCaliph() {
        // This creature attacks each combat if able.
        addEffect(EffectSlot.STATIC, new MustAttackEffect());

        // Whenever this creature deals damage, you lose that much life.
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, new LoseLifeEffect(new EventValue(), LoseLifeRecipient.CONTROLLER, false));
    }
}
