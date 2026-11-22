package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToOwnPermanentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "NEC", collectorNumber = "80")
@CardRegistration(set = "KHM", collectorNumber = "379")
public class ArmedAndArmored extends Card {

    public ArmedAndArmored() {
        addSpellEffect(new AddCardTypeToOwnPermanentsUntilEndOfTurnEffect(
                CardType.CREATURE, new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        addSpellEffect(ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.DWARF)))), "a Dwarf you control"));
        addSpellEffect(new AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect(
                Subjects.thisWay(StepKind.CHOOSE)));
    }
}
