package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardAndConjureOpponentGraveyardDuplicateEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDMU", collectorNumber = "17")
public class NantukoSlicer extends Card {

    public NantukoSlicer() {
        addStaticEffect(new KickerEffect("{B}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ReturnCardAndConjureOpponentGraveyardDuplicateEffect());
    }
}
