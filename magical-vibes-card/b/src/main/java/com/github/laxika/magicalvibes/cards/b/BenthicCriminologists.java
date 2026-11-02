package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "40")
public class BenthicCriminologists extends Card {

    public BenthicCriminologists() {
        MayEffect sacrificeArtifactToDraw = new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentIsArtifactPredicate(), new DrawCardEffect(1), "an artifact", false, false),
                "Sacrifice an artifact?");
        addTrigger(Triggers.enters(ObjectRelation.THIS), sacrificeArtifactToDraw);
        addEffect(EffectSlot.ON_ATTACK, sacrificeArtifactToDraw);
    }
}
