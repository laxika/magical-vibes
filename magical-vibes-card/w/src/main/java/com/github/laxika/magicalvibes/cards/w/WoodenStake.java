package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.DestroySubtypeCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "237")
public class WoodenStake extends Card {

    public WoodenStake() {
        // Equipped creature gets +1/+0
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature blocks or becomes blocked by a Vampire,
        // destroy that creature. It can't be regenerated.
        addTrigger(Triggers.blocks(), new DestroySubtypeCombatOpponentEffect(CardSubtype.VAMPIRE, true));
        addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER,
                new DestroySubtypeCombatOpponentEffect(CardSubtype.VAMPIRE, true));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
