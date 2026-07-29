package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSourceCost;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "253")
public class WallOfRoots extends Card {

    public WallOfRoots() {
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new PutCounterOnSourceCost(0, -1, 1), new AwardManaEffect(ManaColor.GREEN)),
                "Put a -0/-1 counter on this creature: Add {G}. Activate only once each turn.",
                1));
    }
}
