package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "84")
@CardRegistration(set = "MSC", collectorNumber = "405")
public class HawkeyeAvengingArcher extends Card {

    public HawkeyeAvengingArcher() {
        // Whenever a creature an opponent controls dies, if Hawkeye dealt damage to it this turn,
        // draw a card.
        addEffect(EffectSlot.ON_DAMAGED_CREATURE_DIES,
                new TriggeringPermanentConditionalEffect(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new DrawCardEffect()));

        // {T}: Hawkeye deals 1 damage to any target.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{T}: Hawkeye deals 1 damage to any target."));
    }
}
