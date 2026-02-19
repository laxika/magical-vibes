package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllCreaturesYouControlCost;

@CardRegistration(set = "10E", collectorNumber = "236")
public class Soulblast extends Card {

    public Soulblast() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new SacrificeAllCreaturesYouControlCost());
        addEffect(EffectSlot.SPELL, new DealXDamageToAnyTargetEffect());
    }
}
