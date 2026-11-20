package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DST", collectorNumber = "72")
public class VulshokWarBoar extends Card {

    public VulshokWarBoar() {
        // When this creature enters, sacrifice it unless you sacrifice an artifact.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(), Subjects.you()),
                "Sacrifice an artifact? If you don't, sacrifice this creature.", new SacrificeSelfEffect()));
    }
}
