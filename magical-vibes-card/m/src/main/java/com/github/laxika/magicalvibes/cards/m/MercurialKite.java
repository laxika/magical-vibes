package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "SCG", collectorNumber = "39")
public class MercurialKite extends Card {

    public MercurialKite() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
