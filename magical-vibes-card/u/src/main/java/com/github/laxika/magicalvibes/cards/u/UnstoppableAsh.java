package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "137")
public class UnstoppableAsh extends Card {

    public UnstoppableAsh() {
        // Champion a Treefolk or Warrior.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChampionCreatureEffect(CardSubtype.TREEFOLK, CardSubtype.WARRIOR));

        // Whenever a creature you control becomes blocked, it gets +0/+5 until end of turn.
        addTrigger(Triggers.creatureYouControlBecomesBlocked(), new BoostSelfEffect(0, 5));
    }
}
