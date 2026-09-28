package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherCreatureWithManaEffect;

@CardRegistration(set = "MKC", collectorNumber = "6")
@CardRegistration(set = "MKC", collectorNumber = "313")
public class FeatherRadiantArbiter extends Card {

    public FeatherRadiantArbiter() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new CopySpellForEachOtherCreatureWithManaEffect("{2}"));
    }
}
