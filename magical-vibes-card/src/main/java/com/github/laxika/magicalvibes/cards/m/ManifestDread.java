package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;

@CardRegistration(set = "DSK", collectorNumber = "189")
public class ManifestDread extends Card {

    public ManifestDread() {
        addEffect(EffectSlot.SPELL, ManifestDreadEffect.forController());
    }
}
