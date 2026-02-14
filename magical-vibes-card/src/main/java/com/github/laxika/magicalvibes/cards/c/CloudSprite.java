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

public class CloudSprite extends Card {

    public CloudSprite() {
        super("Cloud Sprite", CardType.CREATURE, "{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.FAERIE));
        setCardText("Flying\nCloud Sprite can block only creatures with flying.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(1);
        setToughness(1);
        addEffect(EffectSlot.STATIC, new BlockOnlyFlyersEffect());
    }
}
