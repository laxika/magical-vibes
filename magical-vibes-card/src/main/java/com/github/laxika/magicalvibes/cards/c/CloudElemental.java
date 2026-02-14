package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BlockOnlyFlyersEffect;

import java.util.List;
import java.util.Set;

public class CloudElemental extends Card {

    public CloudElemental() {
        super("Cloud Elemental", CardType.CREATURE, "{2}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.ELEMENTAL));
        setCardText("Flying\nCloud Elemental can block only creatures with flying.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(2);
        setToughness(3);
        addEffect(EffectSlot.STATIC, new BlockOnlyFlyersEffect());
    }
}
