package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;

import java.util.List;
import java.util.Set;

public class ChangelingWayfinder extends Card {

    public ChangelingWayfinder() {
        super("Changeling Wayfinder", CardType.CREATURE, "{3}", null);

        setSubtypes(List.of(CardSubtype.SHAPESHIFTER));
        setCardText("Changeling (This card is every creature type.)\nWhen this creature enters, you may search your library for a basic land card, reveal it, put it into your hand, then shuffle.");
        setKeywords(Set.of(Keyword.CHANGELING));
        setPower(1);
        setToughness(2);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(new SearchLibraryForBasicLandToHandEffect(), "Search your library for a basic land card?"));
    }
}
