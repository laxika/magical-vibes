package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EmbodimentOfFlame;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "141")
@CardRegistration(set = "DBL", collectorNumber = "141")
public class FlameChanneler extends Card {

    public FlameChanneler() {
        setBackFaceCard(new EmbodimentOfFlame());

        addTrigger(Triggers.instantOrSorceryYouControlDealsDamage(), new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "EmbodimentOfFlame";
    }
}
