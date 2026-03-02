package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "8")
public class HeroOfBladehold extends Card {

    public HeroOfBladehold() {
        addEffect(EffectSlot.ON_ATTACK, new CreateCreatureTokenEffect(
                2, "Soldier", 1, 1, CardColor.WHITE, List.of(CardSubtype.SOLDIER), true
        ));
    }
}
