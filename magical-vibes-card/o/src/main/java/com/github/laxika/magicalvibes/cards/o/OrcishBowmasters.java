package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOC", collectorNumber = "19")
@CardRegistration(set = "HOC", collectorNumber = "59")
public class OrcishBowmasters extends Card {

    public OrcishBowmasters() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DealDamageToAnyTargetEffect(1),
                new AmassGoblinsEffect(1, CardSubtype.ORC));
        addTrigger(Triggers.opponentDrawsCardExceptFirstInDrawStep(),
                SequenceEffect.of(
                        new DealDamageToAnyTargetEffect(1),
                        new AmassGoblinsEffect(1, CardSubtype.ORC)));
    }
}
