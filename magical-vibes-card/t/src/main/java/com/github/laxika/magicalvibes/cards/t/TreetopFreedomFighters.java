package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "156")
public class TreetopFreedomFighters extends Card {

    public TreetopFreedomFighters() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Ally",
                1,
                1,
                CardColor.WHITE,
                List.of(CardSubtype.ALLY),
                Set.of(),
                Set.of()
        ));
    }
}
