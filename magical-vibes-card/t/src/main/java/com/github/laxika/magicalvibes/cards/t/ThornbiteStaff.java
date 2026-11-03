package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "145")
@CardRegistration(set = "MB2", collectorNumber = "99")
public class ThornbiteStaff extends Card {

    public ThornbiteStaff() {
        // Equipped creature has "{2}, {T}: This creature deals 1 damage to any target."
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new DealDamageToAnyTargetEffect(1)),
                        "{2}, {T}: This creature deals 1 damage to any target."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        // Equipped creature has "Whenever a creature dies, untap this creature." The granted trigger
        // belongs to the equipped creature, so its controller controls it and it untaps that creature.
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY),
                new UntapPermanentsEffect(TapUntapScope.SELF)),
                GrantScope.EQUIPPED_CREATURE
        ));

        // Whenever a Shaman creature enters, you may attach this Equipment to it.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.SHAMAN))),
                        ObjectRelation.ANOTHER),
                new AttachSourceEquipmentToEnteringCreatureEffect());

        // Equip {4}
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
