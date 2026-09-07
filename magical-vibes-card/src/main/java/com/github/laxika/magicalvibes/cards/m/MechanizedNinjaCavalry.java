package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "156")
public class MechanizedNinjaCavalry extends Card {

    public MechanizedNinjaCavalry() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                1, "Robot", 1, 1, null,
                List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT)));
    }
}
