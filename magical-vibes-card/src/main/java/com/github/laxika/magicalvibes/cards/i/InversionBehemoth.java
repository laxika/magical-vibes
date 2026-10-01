package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M3C", collectorNumber = "34")
public class InversionBehemoth extends Card {

    public InversionBehemoth() {
        // At the beginning of combat on your turn, switch the power and toughness of each of any
        // number of target creatures until end of turn.
        target(TargetFilters.creature(), 0, 100)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new SwitchPowerToughnessEffect());
    }
}
