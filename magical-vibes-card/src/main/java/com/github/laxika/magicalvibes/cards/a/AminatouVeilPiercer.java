package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantMiracleReducedByManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "DSC", collectorNumber = "1")
public class AminatouVeilPiercer extends Card {

    public AminatouVeilPiercer() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SurveilEffect(2));
        addEffect(EffectSlot.STATIC, new GrantMiracleReducedByManaCostEffect(
                new CardTypePredicate(CardType.ENCHANTMENT), 4));
    }
}
