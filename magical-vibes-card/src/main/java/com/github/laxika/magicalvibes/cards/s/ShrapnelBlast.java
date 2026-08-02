package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;

@CardRegistration(set = "M15", collectorNumber = "161")
public class ShrapnelBlast extends Card {

    public ShrapnelBlast() {
        addEffect(EffectSlot.SPELL, new SacrificeArtifactCost());
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(5));
    }
}
