package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "EXO", collectorNumber = "20")
public class SoltariVisionary extends Card {

    public SoltariVisionary() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                new DestroyPermanentDamagedPlayerControlsEffect(new PermanentIsEnchantmentPredicate(), 0));
    }
}
