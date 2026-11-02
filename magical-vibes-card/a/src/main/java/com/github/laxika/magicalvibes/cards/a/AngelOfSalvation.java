package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDividedDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "1")
@CardRegistration(set = "DDF", collectorNumber = "20")
@CardRegistration(set = "TSR", collectorNumber = "3")
@CardRegistration(set = "MOC", collectorNumber = "170")
public class AngelOfSalvation extends Card {

    public AngelOfSalvation() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                PreventDividedDamageEffect.chosenAmongAnyTargetsEtb(5));
    }
}
