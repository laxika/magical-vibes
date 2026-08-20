package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "70")
public class SoulSpike extends Card {

    public SoulSpike() {
        addCastingOption(new AlternateHandCast(List.of(
                new ExileCardsFromHandCastingCost(new CardColorPredicate(CardColor.BLACK), "black", 2))));

        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));
    }
}
