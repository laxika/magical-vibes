package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EmbodimentOfFlame;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "MID", collectorNumber = "141")
public class FlameChanneler extends Card {

    public FlameChanneler() {
        setBackFaceCard(new EmbodimentOfFlame());

        addEffect(EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE, new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "EmbodimentOfFlame";
    }
}
