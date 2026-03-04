package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "134")
public class SphereOfTheSuns extends Card {

    public SphereOfTheSuns() {
        // Sphere of the Suns enters the battlefield tapped and with three charge counters on it.
        setEntersTapped(true);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(3));

        // {T}, Remove a charge counter from Sphere of the Suns: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveChargeCountersFromSourceCost(1),
                        new AwardAnyColorManaEffect()
                ),
                "{T}, Remove a charge counter from Sphere of the Suns: Add one mana of any color."
        ));
    }
}
