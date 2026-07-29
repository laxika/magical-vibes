package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "288")
public class Wellspring extends Card {

    public Wellspring() {
        // Enchant land.
        target(TargetFilters.land())
                // When this Aura enters, gain control of enchanted land until end of turn.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GainControlOfEnchantedPermanentEffect(ControlDuration.END_OF_TURN))
                // At the beginning of your upkeep, untap enchanted land. You gain control of that
                // land until end of turn.
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.ENCHANTED),
                        new GainControlOfEnchantedPermanentEffect(ControlDuration.END_OF_TURN)));
    }
}
