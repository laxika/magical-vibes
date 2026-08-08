package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "74")
public class GoblinTestPilot extends Card {

    public GoblinTestPilot() {
        // The recipient isn't targeted — it's picked at random from every creature,
        // planeswalker and player as the ability resolves.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToRandomAnyTargetEffect(2)),
                "{T}: Goblin Test Pilot deals 2 damage to any target chosen at random."));
    }
}
