package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "72")
public class SquealingDevil extends Card {

    public SquealingDevil() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new PayXManaBoostTargetCreatureEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new NotCondition(new ColorSpentToCast(ManaColor.BLACK)), new SacrificeSelfEffect()));
    }
}
