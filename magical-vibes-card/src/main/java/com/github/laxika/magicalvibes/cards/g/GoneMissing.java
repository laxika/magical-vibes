package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "67")
public class GoneMissing extends Card {

    public GoneMissing() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect())
                .addEffect(EffectSlot.SPELL, CreateTokenEffect.ofClueToken(1));
    }
}
