package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class FragmentOfKonda extends Card {

    public FragmentOfKonda() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect(1));
    }
}
