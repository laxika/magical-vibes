package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "214")
@CardRegistration(set = "4ED", collectorNumber = "179")
@CardRegistration(set = "DRK", collectorNumber = "59")
@CardRegistration(set = "ME1", collectorNumber = "89")
public class BrothersOfFire extends Card {

    public BrothersOfFire() {
        // {1}{R}{R}: This creature deals 1 damage to any target and 1 damage to you.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}{R}",
                List.of(SequenceEffect.simultaneous(DealDamageEffect.toAnyTarget(1), new DealDamageToPlayersEffect(1, Subjects.you()))),
                "{1}{R}{R}: This creature deals 1 damage to any target and 1 damage to you."
        ));
    }
}
