package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.UntapEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "145")
public class ThornbiteStaff extends Card {

    public ThornbiteStaff() {
        // Equipped creature has "{2}, {T}: This creature deals 1 damage to any target."
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new DealDamageToAnyTargetEffect(1)),
                        "{2}, {T}: This creature deals 1 damage to any target."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        // Equipped creature has "Whenever a creature dies, untap this creature". Modeled as a
        // death trigger on the Equipment that untaps the attached creature (fizzles if unattached).
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new UntapEquippedCreatureEffect());

        // Whenever a Shaman creature enters, you may attach this Equipment to it.
        addEffect(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.SHAMAN),
                        new AttachSourceEquipmentToEnteringCreatureEffect()));

        // Equip {4}
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
