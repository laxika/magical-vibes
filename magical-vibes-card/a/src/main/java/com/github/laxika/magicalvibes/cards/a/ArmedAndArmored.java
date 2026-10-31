package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToOwnPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseDwarfAndAttachAnyNumberOfControlledEquipmentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "NEC", collectorNumber = "80")
@CardRegistration(set = "KHM", collectorNumber = "379")
public class ArmedAndArmored extends Card {

    public ArmedAndArmored() {
        addSpellEffect(new AddCardTypeToOwnPermanentsUntilEndOfTurnEffect(
                CardType.CREATURE, new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        addSpellEffect(new ChooseDwarfAndAttachAnyNumberOfControlledEquipmentEffect());
    }
}
