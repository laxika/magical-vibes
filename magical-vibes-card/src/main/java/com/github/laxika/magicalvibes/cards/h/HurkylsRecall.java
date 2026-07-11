package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "88")
public class HurkylsRecall extends Card {

    public HurkylsRecall() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.permanentsTargetPlayerOwns(new PermanentIsArtifactPredicate()));
    }
}
