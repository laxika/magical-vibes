package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "16")
public class HopToIt extends Card {

    public HopToIt() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                3, "Rabbit", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.RABBIT), Set.of(), Set.of()));
    }
}
