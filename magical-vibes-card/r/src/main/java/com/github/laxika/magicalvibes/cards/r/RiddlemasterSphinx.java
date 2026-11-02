package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "287")
@CardRegistration(set = "M20", collectorNumber = "317")
@CardRegistration(set = "ANB", collectorNumber = "31")
public class RiddlemasterSphinx extends Card {

    public RiddlemasterSphinx() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new MayEffect(ReturnToHandEffect.target(),
                                "Return target creature an opponent controls to its owner's hand?"));
    }
}
