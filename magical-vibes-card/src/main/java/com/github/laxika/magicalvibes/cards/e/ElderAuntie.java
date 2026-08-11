package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ECL", collectorNumber = "133")
public class ElderAuntie extends Card {

    public ElderAuntie() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Goblin", 1, 1, CardColor.BLACK,
                Set.of(CardColor.BLACK, CardColor.RED), List.of(CardSubtype.GOBLIN)));
    }
}
