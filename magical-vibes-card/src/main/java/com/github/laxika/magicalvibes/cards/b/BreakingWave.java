package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.SimultaneouslyFlipAllPermanentsTapStatesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "48")
public class BreakingWave extends Card {

    public BreakingWave() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{U}{U}")), null, true));
        addEffect(EffectSlot.SPELL,
                new SimultaneouslyFlipAllPermanentsTapStatesEffect(new PermanentIsCreaturePredicate()));
    }
}
