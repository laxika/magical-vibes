package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "76")
public class ThelonsChant extends Card {

    public ThelonsChant() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {G}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(new PayManaCost("{G}"), List.of(new SacrificeSelfEffect()), true));

        // Whenever a player puts a Swamp onto the battlefield, this enchantment deals 3 damage
        // to that player unless they put a -1/-1 counter on a creature they control.
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.SWAMP),
                        ForcedCostOrElseEffect.enchantedControllerMayPay(
                                new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 1),
                                List.of(new DealDamageToPlayersEffect(3,
                                        DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER)))));
    }
}
