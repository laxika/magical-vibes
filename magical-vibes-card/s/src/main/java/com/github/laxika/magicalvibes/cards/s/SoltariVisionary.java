package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "20")
public class SoltariVisionary extends Card {

    public SoltariVisionary() {
        addTrigger(Triggers.dealsDamageToPlayer(),
                new DestroyPermanentDamagedPlayerControlsEffect(new PermanentIsEnchantmentPredicate(), 0));
    }
}
