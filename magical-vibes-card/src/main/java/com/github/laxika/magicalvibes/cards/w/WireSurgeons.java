package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEncoreToArtifactCreatureCardsEffect;

@CardRegistration(set = "BRC", collectorNumber = "10")
@CardRegistration(set = "BRC", collectorNumber = "57")
public class WireSurgeons extends Card {

    public WireSurgeons() {
        addEffect(EffectSlot.STATIC, new GrantEncoreToArtifactCreatureCardsEffect());
    }
}
