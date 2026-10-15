package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "183")
public class ElementalSummoning extends Card {

    public ElementalSummoning() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Elemental", 4, 4, CardColor.BLUE,
                Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.ELEMENTAL)));
    }
}
