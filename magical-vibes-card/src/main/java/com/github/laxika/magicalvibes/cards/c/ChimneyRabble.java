package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "126")
public class ChimneyRabble extends Card {

    public ChimneyRabble() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Phyrexian Goblin", 1, 1, CardColor.RED,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
