package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "237")
public class WoodenStake extends Card {

    public WoodenStake() {
        // Equipped creature gets +1/+0
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature blocks or becomes blocked by a Vampire, destroy that creature. It can't be
        // regenerated. The filter is on the creature on the other side of the block.
        PermanentHasSubtypePredicate vampire = new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE);
        addTrigger(Triggers.blocks().withFilter(vampire), new DestroyCombatOpponentEffect(true));
        addTrigger(Triggers.becomesBlocked().withFilter(vampire),
                TriggerOptions.EACH_BLOCKER,
                new DestroyCombatOpponentEffect(true));

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
