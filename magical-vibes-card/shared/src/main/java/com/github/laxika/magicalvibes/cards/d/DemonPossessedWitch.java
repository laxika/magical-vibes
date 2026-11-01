package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class DemonPossessedWitch extends Card {

    public DemonPossessedWitch() {
        target(TargetFilters.creature());
        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS),
                new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target creature?"));
    }
}
