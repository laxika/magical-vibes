package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Set;

public class AngelOfMercy extends Card {

    public AngelOfMercy() {
        super("Angel of Mercy", CardType.CREATURE, "{4}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.ANGEL));
        setCardText("Flying\nWhen Angel of Mercy enters the battlefield, you gain 3 life.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(3);
        setToughness(3);
        setOnEnterBattlefieldEffects(List.of(new GainLifeEffect(3)));
    }
}
