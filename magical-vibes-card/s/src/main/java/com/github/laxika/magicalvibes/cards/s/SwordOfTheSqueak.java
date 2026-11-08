package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBasePowerEqualsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentBaseToughnessEqualsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "40")
@CardRegistration(set = "BLC", collectorNumber = "72")
public class SwordOfTheSqueak extends Card {

    public SwordOfTheSqueak() {
        PermanentAnyOfPredicate basePowerOrToughnessOne = new PermanentAnyOfPredicate(List.of(
                new PermanentBasePowerEqualsPredicate(1),
                new PermanentBaseToughnessEqualsPredicate(1)));
        PermanentCount qualifyingCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        basePowerOrToughnessOne)),
                CountScope.CONTROLLER);
        addStaticEffect(new DynamicStaticBoostEffect(
                qualifyingCreatures, qualifyingCreatures, GrantScope.EQUIPPED_CREATURE));

        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentHasSubtypePredicate(CardSubtype.HAMSTER),
                                        new PermanentHasSubtypePredicate(CardSubtype.MOUSE),
                                        new PermanentHasSubtypePredicate(CardSubtype.RAT),
                                        new PermanentHasSubtypePredicate(CardSubtype.SQUIRREL))))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature(),
                        "Attach this Equipment to it?"));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
