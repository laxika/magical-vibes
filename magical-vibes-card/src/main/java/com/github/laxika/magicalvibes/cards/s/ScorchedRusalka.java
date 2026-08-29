package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "74")
public class ScorchedRusalka extends Card {

    public ScorchedRusalka() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeCreatureCost(), new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "{R}, Sacrifice a creature: This creature deals 1 damage to target player or planeswalker."
        ));
    }
}
