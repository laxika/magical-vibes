package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IslandwalkEffect;

import java.util.List;

public class RootwaterCommando extends Card {

    public RootwaterCommando() {
        super("Rootwater Commando", CardType.CREATURE, "{2}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.MERFOLK));
        setCardText("Islandwalk (This creature can't be blocked as long as defending player controls an Island.)");
        setPower(2);
        setToughness(2);
        addEffect(EffectSlot.STATIC, new IslandwalkEffect());
    }
}
