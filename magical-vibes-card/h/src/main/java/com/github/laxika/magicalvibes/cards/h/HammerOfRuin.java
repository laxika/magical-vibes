package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "124")
public class HammerOfRuin extends Card {

    public HammerOfRuin() {
        // Equipped creature gets +2/+0.
        addStaticEffect(new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature deals combat damage to a player, you may destroy target
        // Equipment that player controls.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                        new PermanentControlledByTriggeringPlayerPredicate()))),
                        "You may destroy target Equipment that player controls."));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
