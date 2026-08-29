package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "23")
public class SagesReverie extends Card {

    public SagesReverie() {
        PermanentCount aurasAttachedToCreatures = new PermanentCount(
                new PermanentIsAuraAttachedToCreaturePredicate(), CountScope.CONTROLLER);

        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(aurasAttachedToCreatures))
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        aurasAttachedToCreatures, aurasAttachedToCreatures, GrantScope.ENCHANTED_CREATURE));
    }
}
