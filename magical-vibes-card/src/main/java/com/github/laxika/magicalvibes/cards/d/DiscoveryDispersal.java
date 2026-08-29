package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentReturnsGreatestManaValueNonlandPermanentThenDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilThenEffect;
import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "223")
public class DiscoveryDispersal extends Card {

    public DiscoveryDispersal() {
        CardEffect discovery = new SurveilThenEffect(2, new DrawCardEffect(1));
        CardEffect dispersal = new EachOpponentReturnsGreatestManaValueNonlandPermanentThenDiscardsEffect();

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Discovery — Surveil 2, then draw a card", discovery).withManaCost("{1}{U/B}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Dispersal — Each opponent returns a nonland permanent they control with the greatest mana value among permanents they control to its owner's hand, then discards a card",
                        dispersal).withManaCost("{3}{U}{B}")
        )));
    }
}
