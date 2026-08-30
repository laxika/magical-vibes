package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardEffect;

@CardRegistration(set = "FRF", collectorNumber = "123")
public class ArashinWarBeast extends Card {

    public ArashinWarBeast() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_BLOCKING_CREATURE, new ManifestTopCardEffect());
    }
}
