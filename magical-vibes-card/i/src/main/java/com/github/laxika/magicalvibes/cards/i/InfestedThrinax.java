package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "74")
@CardRegistration(set = "M3C", collectorNumber = "126")
public class InfestedThrinax extends Card {

    public InfestedThrinax() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(
                        new TriggeredAbility(Triggers.nontokenCreatureDies(ObjectRelation.YOU_CONTROL),
                        new CreateTokenEffect(new EventValue(), "Saproling", 1, 1,
                                CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of()))));
    }
}
