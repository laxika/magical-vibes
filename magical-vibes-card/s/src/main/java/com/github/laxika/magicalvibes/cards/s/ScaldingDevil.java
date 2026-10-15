package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "155")
public class ScaldingDevil extends Card {

    public ScaldingDevil() {
        // {2}{R}: This creature deals 1 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(false, "{2}{R}",
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "{2}{R}: This creature deals 1 damage to target player or planeswalker."));
    }
}
