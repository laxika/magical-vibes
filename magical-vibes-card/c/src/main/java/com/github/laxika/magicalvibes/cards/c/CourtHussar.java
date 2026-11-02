package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "22")
@CardRegistration(set = "GK2", collectorNumber = "5")
@CardRegistration(set = "A25", collectorNumber = "51")
@CardRegistration(set = "CMD", collectorNumber = "43")
public class CourtHussar extends Card {

    public CourtHussar() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)));
        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT, ConditionalEffect.unless(
                new NotCondition(new ColorSpentToCast(ManaColor.WHITE)), new SacrificeSelfEffect()));
    }
}
