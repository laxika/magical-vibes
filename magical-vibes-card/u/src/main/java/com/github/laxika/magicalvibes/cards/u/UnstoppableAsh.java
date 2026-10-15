package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;

@CardRegistration(set = "MOR", collectorNumber = "137")
public class UnstoppableAsh extends Card {

    public UnstoppableAsh() {
        // Champion a Treefolk or Warrior.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChampionCreatureEffect(CardSubtype.TREEFOLK, CardSubtype.WARRIOR));

        // Whenever a creature you control becomes blocked, it gets +0/+5 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_BLOCKED, new BoostSelfEffect(0, 5));
    }
}
