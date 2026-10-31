package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "EMN", collectorNumber = "73")
public class ScourTheLaboratory extends Card {

    public ScourTheLaboratory() {
        // Delirium — This spell costs {2} less to cast if there are four or more card types among
        // cards in your graveyard.
        addStaticEffect(new ConditionalEffect(
                new Delirium(), new ReduceOwnCastCostEffect(new Fixed(2))));

        // Draw three cards.
        addSpellEffect(new DrawCardEffect(3));
    }
}
