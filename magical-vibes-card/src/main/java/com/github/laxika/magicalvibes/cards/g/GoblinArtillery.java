package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "138")
public class GoblinArtillery extends Card {

    public GoblinArtillery() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToAnyTargetEffect(2), new DealDamageToControllerEffect(3)),
                "{T}: Goblin Artillery deals 2 damage to any target and 3 damage to you."
        ));
    }
}
