package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;

import java.util.List;

public class HuntedWumpus extends Card {

    public HuntedWumpus() {
        super("Hunted Wumpus", CardType.CREATURE, "{3}{G}");

        setSubtypes(List.of(CardSubtype.BEAST));
        setCardText("When Hunted Wumpus enters the battlefield, each other player may put a creature card from their hand onto the battlefield.");
        setPower(6);
        setToughness(4);
        setOnEnterBattlefieldEffects(List.of(new OpponentMayPlayCreatureEffect()));
    }
}
