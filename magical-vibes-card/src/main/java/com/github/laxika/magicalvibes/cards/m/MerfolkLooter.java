package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

public class MerfolkLooter extends Card {

    public MerfolkLooter() {
        super("Merfolk Looter", CardType.CREATURE, "{1}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.MERFOLK, CardSubtype.ROGUE));
        setCardText("{T}: Draw a card, then discard a card.");
        setPower(1);
        setToughness(1);
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DrawCardEffect(), new DiscardCardEffect()), false, "{T}: Draw a card, then discard a card."));
    }
}
