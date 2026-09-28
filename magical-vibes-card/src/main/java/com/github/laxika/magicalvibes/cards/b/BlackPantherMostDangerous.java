package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "584")
public class BlackPantherMostDangerous extends Card {

    public BlackPantherMostDangerous() {
        // Whenever Black Panther is dealt damage, he deals that much damage to any other target.
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                DealDamageToAnyTargetEffect.toAnyOtherTarget(new EventValue()));

        // Other creatures you control get +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{W}{W}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new BoostAllOwnCreaturesEffect(2, 2,
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))
                ),
                "Power-up — {5}{W}{W}: Put two +1/+1 counters on Black Panther. Other creatures you control get +2/+2 "
                        + "until end of turn. Activate each power-up ability only once. Reduce the cost by his mana cost "
                        + "if he entered this turn."
        ).withPowerUp());
    }
}
