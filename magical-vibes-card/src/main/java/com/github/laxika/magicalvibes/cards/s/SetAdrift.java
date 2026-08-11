package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "54")
public class SetAdrift extends Card {

    public SetAdrift() {
        addEffect(EffectSlot.SPELL, new DelveCost());
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect());
    }
}
