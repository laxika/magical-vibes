package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "96")
public class StompAndHowl extends Card {

    public StompAndHowl() {
        setAllowSharedTargets(true);

        DestroyTargetPermanentEffect destroy = new DestroyTargetPermanentEffect();
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, destroy);
        target(TargetFilters.enchantment()).bindEffect(destroy);
    }
}
