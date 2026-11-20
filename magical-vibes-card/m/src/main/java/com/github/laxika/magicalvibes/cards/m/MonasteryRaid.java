package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.Freerunning;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "35")
public class MonasteryRaid extends Card {

    public MonasteryRaid() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{X}{R}")), new Freerunning(), false));

        // This card's only alternate cost is freerunning, so CastForAlternateCost identifies
        // whether its freerunning cost was paid at resolution.
        addSpellEffect(new ConditionalEffect(
                new CastForAlternateCost(), new ExileTopCardMayPlayThisTurnEffect(new XValue(), false)
                        .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)));
        addSpellEffect(new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()), new ExileTopCardMayPlayThisTurnEffect(2, false)
                        .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)));
    }
}
