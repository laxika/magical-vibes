package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "EVE", collectorNumber = "148")
public class WavesOfAggression extends Card {

    public WavesOfAggression() {
        addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES));
        addEffect(EffectSlot.SPELL, new AdditionalCombatMainPhaseEffect(1));
        addCastingOption(new Retrace());
    }
}
