package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureWithLeastPowerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Drop of Honey — {G} Enchantment.
 * Destroys a creature with the least power during its controller's upkeep and sacrifices itself
 * when there are no creatures on the battlefield.
 */
@CardRegistration(set = "ME4", collectorNumber = "150")
@CardRegistration(set = "ARN", collectorNumber = "47")
public class DropOfHoney extends Card {

    public DropOfHoney() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DestroyCreatureWithLeastPowerEffect(true));

        addTrigger(Triggers.state(), StateTriggerEffect.whenBattlefieldHasAtMost(0,
                new PermanentIsCreaturePredicate(),
                List.of(new SacrificeSelfEffect()),
                "Drop of Honey's state-triggered ability"
        ));
    }
}
