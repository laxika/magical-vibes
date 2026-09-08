package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardThenMayPutPermanentWithManaValueAtMostLandsEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "1")
public class AnticausalVestige extends Card {

    public AnticausalVestige() {
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new DrawCardThenMayPutPermanentWithManaValueAtMostLandsEffect());
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}"))));
    }
}
