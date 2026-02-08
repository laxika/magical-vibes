package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;

import java.util.List;
import java.util.Set;

public class HuntedWumpus extends Card {

    public HuntedWumpus() {
        super("Hunted Wumpus", CardType.CREATURE, List.of(CardSubtype.BEAST), null, List.of(), "{3}{G}", 6, 4, Set.of(), List.of(new OpponentMayPlayCreatureEffect()));
    }
}
