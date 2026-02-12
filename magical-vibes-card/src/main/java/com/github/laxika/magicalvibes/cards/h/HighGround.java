package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;

import java.util.List;

public class HighGround extends Card {

    public HighGround() {
        super("High Ground", CardType.ENCHANTMENT, "{W}", CardColor.WHITE);

        setCardText("Each creature you control can block an additional creature each combat.");
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));
    }
}
