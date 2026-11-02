package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "81")
public class GlowingAnemone extends Card {

    public GlowingAnemone() {
        target(TargetFilters.land()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(ReturnToHandEffect.target(),
                        "Return target land to its owner's hand?"));
    }
}
