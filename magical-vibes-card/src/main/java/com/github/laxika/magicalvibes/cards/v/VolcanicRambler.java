package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "167")
public class VolcanicRambler extends Card {

    public VolcanicRambler() {
        // {2}{R}: This creature deals 1 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(false, "{2}{R}",
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "{2}{R}: This creature deals 1 damage to target player or planeswalker."));
    }
}
