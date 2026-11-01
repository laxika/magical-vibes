package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndAttachTriggeringAuraEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M14", collectorNumber = "2")
@CardRegistration(set = "SOC", collectorNumber = "132")
@CardRegistration(set = "C15", collectorNumber = "57")
@CardRegistration(set = "WOC", collectorNumber = "59")
@CardRegistration(set = "C18", collectorNumber = "61")
public class AjanisChosen extends Card {

    public AjanisChosen() {
        // Whenever an enchantment you control enters, create a 2/2 white Cat creature token.
        // If that enchantment is an Aura, you may attach it to the token.
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new CreateTokenAndAttachTriggeringAuraEffect(
                        new CreateTokenEffect("Cat", 2, 2, CardColor.WHITE,
                                List.of(CardSubtype.CAT), Set.of(), Set.of())));
    }
}
