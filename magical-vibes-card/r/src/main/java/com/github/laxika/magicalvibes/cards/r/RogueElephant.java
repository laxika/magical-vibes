package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "139")
public class RogueElephant extends Card {

    public RogueElephant() {
        // When this creature enters, sacrifice it unless you sacrifice a Forest.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SacrificePermanentsEffect(1, new PermanentHasSubtypePredicate(CardSubtype.FOREST), Subjects.you()),
                "Sacrifice a Forest? If you don't, sacrifice this creature.", new SacrificeSelfEffect()));
    }
}
