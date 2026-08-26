package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardXCardsCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "151")
public class Conflagrate extends Card {

    public Conflagrate() {
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(new XValue()));
        addCastingOption(new FlashbackCast(List.of(
                new ManaCastingCost("{R}{R}"),
                new DiscardXCardsCastingCost())));
    }
}
