package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "117")
public class Vaultbreaker extends Card {

    public Vaultbreaker() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnSelfToHandIfDashCostPaidEffect());
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
