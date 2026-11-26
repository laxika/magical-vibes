package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "FEM", collectorNumber = "76")
public class ThelonsChant extends Card {

    public ThelonsChant() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {G}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{G}", new SacrificeSelfEffect()));

        // Whenever a player puts a Swamp onto the battlefield, this enchantment deals 3 damage
        // to that player unless they put a -1/-1 counter on a creature they control.
        addTrigger(Triggers.enters(new PermanentHasSubtypePredicate(CardSubtype.SWAMP), ObjectRelation.ANY),
                UnlessPaysEffect.unlessPays(Subjects.controllerOf(Subjects.triggering()),
                        new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 1),
                        DealDamageEffect.to(Subjects.controllerOf(Subjects.triggering()), 3))
                        .withPrompt("Put a -1/-1 counter on a creature you control?"));
    }
}
