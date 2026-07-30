package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "139")
public class GoblinFireslinger extends Card {

    public GoblinFireslinger() {
        // {T}: This creature deals 1 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "{T}: Goblin Fireslinger deals 1 damage to target player or planeswalker."));
    }
}
