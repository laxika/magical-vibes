package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "29")
public class LightOfDay extends Card {

    public LightOfDay() {
        // Black creatures can't attack or block.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantAttackOrBlockEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                "Black creatures can't attack or block"));
    }
}
