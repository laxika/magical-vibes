package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THB", collectorNumber = "33")
public class ReverentHoplite extends Card {

    public ReverentHoplite() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.WHITE),
                "Human Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()));
    }
}
