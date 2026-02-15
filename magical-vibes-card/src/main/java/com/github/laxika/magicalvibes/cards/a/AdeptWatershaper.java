package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToOwnTappedCreaturesEffect;

import java.util.List;

public class AdeptWatershaper extends Card {

    public AdeptWatershaper() {
        super("Adept Watershaper", CardType.CREATURE, "{2}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.MERFOLK, CardSubtype.CLERIC));
        setCardText("Other tapped creatures you control have indestructible.");
        setPower(3);
        setToughness(4);
        addEffect(EffectSlot.STATIC, new GrantKeywordToOwnTappedCreaturesEffect(Keyword.INDESTRUCTIBLE));
    }
}
