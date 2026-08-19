package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLS", collectorNumber = "79")
public class FallingTimber extends Card {

    public FallingTimber() {
        addEffect(EffectSlot.STATIC, new KickerEffect(new PermanentIsLandPredicate(), "a land"));
        targetWhenKicked(TargetFilters.creature(), 1, 1, 2, 2)
                .addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombatByTargetCreatures());
    }
}
