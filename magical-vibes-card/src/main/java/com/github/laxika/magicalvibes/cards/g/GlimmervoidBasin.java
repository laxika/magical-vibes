package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPermanentOrPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetCreatureForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OHOP", collectorNumber = "12")
public class GlimmervoidBasin extends Card {

    public GlimmervoidBasin() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new CopySpellForEachOtherPermanentOrPlayerEffect());
        target(TargetFilters.creature()).addEffect(EffectSlot.CHAOS_TRIGGERED,
                new CreateTokenCopyOfTargetCreatureForEachOtherPlayerEffect());
    }
}
