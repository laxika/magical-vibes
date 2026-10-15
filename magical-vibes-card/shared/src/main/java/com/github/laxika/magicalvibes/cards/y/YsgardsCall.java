package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

public class YsgardsCall extends Card {

    public YsgardsCall() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new XValue(), "Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.SOLDIER), Set.of(), Set.of()));
    }
}
