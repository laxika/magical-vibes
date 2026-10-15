package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

public class AtTheDoor extends Card {

    public AtTheDoor() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new XValue(), "Dwarf", 2, 2, CardColor.RED,
                List.of(CardSubtype.DWARF), Set.of(), Set.of()));
    }
}
