package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "218")
public class SkyknightVanguard extends Card {

    public SkyknightVanguard() {
        addEffect(EffectSlot.ON_ATTACK, new CreateTokenEffect(
                1, "Soldier", 1, 1, CardColor.WHITE, List.of(CardSubtype.SOLDIER), true
        ));
    }
}
