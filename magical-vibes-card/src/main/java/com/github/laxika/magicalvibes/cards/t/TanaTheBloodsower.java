package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C16", collectorNumber = "45")
public class TanaTheBloodsower extends Card {

    public TanaTheBloodsower() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new CreateTokenEffect(
                new EventValue(), "Saproling", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.SAPROLING), Set.of(), Set.of()));
    }
}
