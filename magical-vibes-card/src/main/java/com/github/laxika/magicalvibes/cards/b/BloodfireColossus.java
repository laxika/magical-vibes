package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAllCreaturesAndPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

public class BloodfireColossus extends Card {

    public BloodfireColossus() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToAllCreaturesAndPlayersEffect(6)),
                false,
                "{R}, Sacrifice Bloodfire Colossus: It deals 6 damage to each creature and each player."
        ));
    }
}
