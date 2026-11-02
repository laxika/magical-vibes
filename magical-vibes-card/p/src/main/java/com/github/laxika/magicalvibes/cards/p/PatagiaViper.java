package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DIS", collectorNumber = "89")
@CardRegistration(set = "C15", collectorNumber = "197")
public class PatagiaViper extends Card {

    public PatagiaViper() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenEffect(2, "Snake", 1, 1, null,
                        Set.of(CardColor.GREEN, CardColor.BLUE), List.of(CardSubtype.SNAKE)));
        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT, ConditionalEffect.unless(
                new NotCondition(new ColorSpentToCast(ManaColor.BLUE)), new SacrificeSelfEffect()));
    }
}
