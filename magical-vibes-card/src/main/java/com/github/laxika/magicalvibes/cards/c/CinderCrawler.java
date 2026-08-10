package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.BlockedByMinCreatures;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "80")
public class CinderCrawler extends Card {

    public CinderCrawler() {
        // {R}: This creature gets +1/+0 until end of turn. Activate only if this creature is blocked.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn. Activate only if this creature is blocked."
        ).withActivationCondition(
                new BlockedByMinCreatures(1),
                "Activate only if this creature is blocked"
        ));
    }
}
