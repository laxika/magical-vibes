package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

import java.util.List;
import java.util.Set;

public class AvenFisher extends Card {

    public AvenFisher() {
        super("Aven Fisher", CardType.CREATURE, "{3}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.BIRD, CardSubtype.SOLDIER));
        setCardText("Flying\nWhen Aven Fisher dies, you may draw a card.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(2);
        setToughness(2);
        addEffect(EffectSlot.ON_DEATH, new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
