package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.Freerunning;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "35")
public class MonasteryRaid extends Card {

    public MonasteryRaid() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{X}{R}")), new Freerunning(), false));

        // This card's only alternate cost is freerunning, so CastForAlternateCost identifies
        // whether its freerunning cost was paid at resolution.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CastForAlternateCost(), new ExileTopCardsMayPlayUntilNextTurnEffect(new XValue())));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()), new ExileTopCardsMayPlayUntilNextTurnEffect(2)));
    }
}
