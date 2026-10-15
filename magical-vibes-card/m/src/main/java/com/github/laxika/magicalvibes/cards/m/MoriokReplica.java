package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "178")
public class MoriokReplica extends Card {

    public MoriokReplica() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(2), new LoseLifeEffect(2)),
                "{1}{B}, Sacrifice Moriok Replica: You draw two cards and you lose 2 life."
        ));
    }
}
