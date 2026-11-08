package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "132")
public class FruitOfTheFirstTree extends Card {

    public FruitOfTheFirstTree() {
        target(TargetFilters.creature())
                // When enchanted creature dies, you gain X life and draw X cards, where X is its toughness.
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED), SequenceEffect.of(
                        new GainLifeEffect(new TriggeringPermanentToughness()),
                        new DrawCardEffect(new TriggeringPermanentToughness())));
    }
}
