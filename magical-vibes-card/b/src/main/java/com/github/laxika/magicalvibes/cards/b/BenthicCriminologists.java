package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "40")
public class BenthicCriminologists extends Card {

    public BenthicCriminologists() {
        MayEffect sacrificeArtifactToDraw = new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new DrawCardEffect(1))),
                "Sacrifice an artifact?");
        addTrigger(Triggers.enters(ObjectRelation.THIS), sacrificeArtifactToDraw);
        addTrigger(Triggers.attacks(), sacrificeArtifactToDraw);
    }
}
