package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DKA", collectorNumber = "17")
@CardRegistration(set = "JUD", collectorNumber = "20")
public class RayOfRevelation extends Card {

    public RayOfRevelation() {
        target(TargetFilters.enchantment()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        addCastingOption(new FlashbackCast("{G}"));
    }
}
