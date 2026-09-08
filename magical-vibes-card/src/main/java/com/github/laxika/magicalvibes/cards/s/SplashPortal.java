package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "74")
public class SplashPortal extends Card {

    public SplashPortal() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, FlickerEffect.flickerTargetWithBonus(
                        Set.of(CardSubtype.BIRD, CardSubtype.FROG, CardSubtype.OTTER, CardSubtype.RAT),
                        new DrawCardEffect(1)));
    }
}
