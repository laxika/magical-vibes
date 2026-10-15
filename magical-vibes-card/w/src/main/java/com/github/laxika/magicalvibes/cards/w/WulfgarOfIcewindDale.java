package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "AFC", collectorNumber = "56")
public class WulfgarOfIcewindDale extends Card {

    public WulfgarOfIcewindDale() {
        addEffect(EffectSlot.STATIC, AdditionalTriggeredAbilityEffect.forAttackTriggers(
                new PermanentTruePredicate(), null));
    }
}
