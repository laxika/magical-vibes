package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "5")
public class MascotExhibition extends Card {

    public MascotExhibition() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Inkling", 2, 1, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK), List.of(CardSubtype.INKLING),
                Set.of(Keyword.FLYING), Set.of()));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Spirit", 3, 2, CardColor.RED,
                Set.of(CardColor.RED, CardColor.WHITE), List.of(CardSubtype.SPIRIT)));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Elemental", 4, 4, CardColor.BLUE,
                Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.ELEMENTAL)));
    }
}
