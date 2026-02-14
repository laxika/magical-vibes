package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;

import java.util.List;
import java.util.Set;

public class SageOwl extends Card {

    public SageOwl() {
        super("Sage Owl", CardType.CREATURE, "{1}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.BIRD));
        setCardText("Flying\nWhen Sage Owl enters the battlefield, look at the top four cards of your library, then put them back in any order.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(1);
        setToughness(1);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReorderTopCardsOfLibraryEffect(4));
    }
}
