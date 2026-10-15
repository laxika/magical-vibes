package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "4")
public class BeskirShieldmate extends Card {

    public BeskirShieldmate() {
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1, "Human Warrior", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.WARRIOR), Set.of(), Set.of()));
    }
}
