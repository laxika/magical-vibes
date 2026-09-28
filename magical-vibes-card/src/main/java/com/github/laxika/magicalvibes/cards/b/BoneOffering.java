package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

public class BoneOffering extends Card {

    public BoneOffering() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1,
                "Skeleton",
                4,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.SKELETON),
                Set.of(Keyword.MENACE),
                Set.of(),
                true
        ));
    }
}
