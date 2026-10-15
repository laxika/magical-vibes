package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DST", collectorNumber = "82")
public class RebukingCeremony extends Card {

    public RebukingCeremony() {
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect());
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect());
    }
}
