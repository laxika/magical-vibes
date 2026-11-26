package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

// Tutorial-only card: neither oracle provider supplies a supported printing to register.
public class OgrePainbringer extends Card {

    public OgrePainbringer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                DealDamageEffect.to(Subjects.eachPlayer(), 3));
    }
}
