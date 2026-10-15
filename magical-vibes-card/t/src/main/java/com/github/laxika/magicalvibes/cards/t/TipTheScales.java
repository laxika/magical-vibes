package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentToughness;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TDC", collectorNumber = "29")
@CardRegistration(set = "TDC", collectorNumber = "69")
public class TipTheScales extends Card {

    public TipTheScales() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentThenEffect(
                new PermanentIsCreaturePredicate(),
                new BoostAllCreaturesEffect(
                        new Scaled(new SacrificedPermanentToughness(), -1),
                        new Scaled(new SacrificedPermanentToughness(), -1)),
                "a creature"));
    }
}
