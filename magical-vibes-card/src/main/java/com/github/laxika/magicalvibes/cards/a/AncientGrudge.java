package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "127")
@CardRegistration(set = "TSP", collectorNumber = "143")
public class AncientGrudge extends Card {

    public AncientGrudge() {
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        addCastingOption(new FlashbackCast("{G}"));
    }
}
