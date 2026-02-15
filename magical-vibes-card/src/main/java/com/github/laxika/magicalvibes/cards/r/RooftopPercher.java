package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

import java.util.List;
import java.util.Set;

public class RooftopPercher extends Card {

    public RooftopPercher() {
        super("Rooftop Percher", CardType.CREATURE, "{5}", null);

        setSubtypes(List.of(CardSubtype.SHAPESHIFTER));
        setCardText("Changeling (This card is every creature type.)\nFlying\nWhen this creature enters, exile up to two target cards from graveyards. You gain 3 life.");
        setKeywords(Set.of(Keyword.CHANGELING, Keyword.FLYING));
        setPower(3);
        setToughness(3);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileCardsFromGraveyardEffect(2, 3));
    }
}
