package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "321")
public class TelimTorsDarts extends Card {

    public TelimTorsDarts() {
        // {2}, {T}: This artifact deals 1 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(true, "{2}",
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "{2}, {T}: This artifact deals 1 damage to target player or planeswalker."));
    }
}
