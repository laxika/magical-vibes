package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RedirectTargetCreatureDamageFromChosenSourceToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLC", collectorNumber = "87")
public class KorDirge extends Card {

    public KorDirge() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new RedirectTargetCreatureDamageFromChosenSourceToTargetEffect());
    }
}
