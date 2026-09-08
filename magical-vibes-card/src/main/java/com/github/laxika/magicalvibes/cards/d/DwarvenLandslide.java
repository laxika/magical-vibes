package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "APC", collectorNumber = "60")
public class DwarvenLandslide extends Card {

    public DwarvenLandslide() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{R}", new PermanentIsLandPredicate(), "a land"));

        target(TargetFilters.land())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        targetWhenKicked(TargetFilters.land(), 0, 0, 1, 1)
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new Kicked(), new DestroyTargetPermanentEffect()));
    }
}
