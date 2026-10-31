package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

@CardRegistration(set = "ROE", collectorNumber = "146")
@CardRegistration(set = "DDP", collectorNumber = "60")
public class ForkedBolt extends Card {

    public ForkedBolt() {
        // Forked Bolt deals 2 damage divided as you choose among one or two targets.
        target(1, 2).addSpellEffect(DealDividedDamageEffect.chosenAmongAnyTargets(2));
    }
}
