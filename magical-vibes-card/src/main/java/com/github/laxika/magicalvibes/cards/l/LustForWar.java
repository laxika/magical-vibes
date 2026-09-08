package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "157")
public class LustForWar extends Card {

    public LustForWar() {
        target(TargetFilters.creature());
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                new DealDamageToPlayersEffect(3, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
