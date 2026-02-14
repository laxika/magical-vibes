package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillByHandSizeEffect;

import java.util.List;

public class DreambornMuse extends Card {

    public DreambornMuse() {
        super("Dreamborn Muse", CardType.CREATURE, "{2}{U}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.SPIRIT));
        setCardText("At the beginning of each player's upkeep, that player mills X cards, where X is the number of cards in their hand.");
        setPower(2);
        setToughness(2);
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new MillByHandSizeEffect());
    }
}
