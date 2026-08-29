package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AER", collectorNumber = "36")
public class IllusionistsStratagem extends Card {

    public IllusionistsStratagem() {
        target(TargetFilters.creatureYouControl(), 0, 2)
                .addEffect(EffectSlot.SPELL, FlickerEffect.flickerTarget())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
