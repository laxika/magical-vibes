package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect;

@CardRegistration(set = "MBS", collectorNumber = "69")
@CardRegistration(set = "2XM", collectorNumber = "134")
public class KuldothaFlamefiend extends Card {

    public KuldothaFlamefiend() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SacrificeArtifactThenDealDividedDamageEffect(4), "Sacrifice an artifact?"));
    }
}
