package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "IKO", collectorNumber = "8")
public class DaysquadMarshal extends Card {

    public DaysquadMarshal() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect("Human Soldier", 1, 1,
                CardColor.WHITE, List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER), Set.of(), Set.of()));
    }
}
