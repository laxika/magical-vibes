package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetedBySpellColorsEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "272")
public class KarplusanStrider extends Card {

    public KarplusanStrider() {
        addEffect(EffectSlot.STATIC, new CantBeTargetedBySpellColorsEffect(Set.of(CardColor.BLUE, CardColor.BLACK)));
    }
}
