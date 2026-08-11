package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "26")
public class TakeUpArms extends Card {

    public TakeUpArms() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                3, "Warrior", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.WARRIOR), Set.of(), Set.of()));
    }
}
