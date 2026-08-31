package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHR", collectorNumber = "37")
@CardRegistration(set = "LEG", collectorNumber = "119")
public class Takklemaggot extends Card {

    public Takklemaggot() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                        new PutCounterOnReferencedPermanentEffect(CounterType.MINUS_ZERO_MINUS_ONE))
                .addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                        new ReturnSourceAuraToCreatureOrNonAuraOnDeathEffect());
    }
}
