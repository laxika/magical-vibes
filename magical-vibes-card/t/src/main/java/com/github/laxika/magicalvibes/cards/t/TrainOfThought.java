package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "39")
@CardRegistration(set = "DDJ", collectorNumber = "19")
public class TrainOfThought extends Card {

    public TrainOfThought() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{1}{U}")));
        addSpellEffect(new DrawCardEffect(1));
        addTrigger(Triggers.youCastThisSpell(), new ReplicateEffect("{1}{U}"));
    }
}
