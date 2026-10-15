package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageBySelfToCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "133")
public class IndenturedOaf extends Card {

    public IndenturedOaf() {
        addEffect(EffectSlot.STATIC, new PreventDamageBySelfToCreaturesEffect(
                new PermanentColorInPredicate(Set.of(CardColor.RED))));
    }
}
