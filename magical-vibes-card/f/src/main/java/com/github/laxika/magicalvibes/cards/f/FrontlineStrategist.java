package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "15")
public class FrontlineStrategist extends Card {

    public FrontlineStrategist() {
        addMorph("{W}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                PreventDamageEffect.allCombatExcept(new PermanentHasSubtypePredicate(CardSubtype.SOLDIER)));
    }
}
