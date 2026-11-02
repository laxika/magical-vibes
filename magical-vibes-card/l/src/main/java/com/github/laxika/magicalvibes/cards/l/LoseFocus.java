package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReplicateEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "49")
public class LoseFocus extends Card {

    public LoseFocus() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{U}")));
        addSpellEffect(new CounterUnlessPaysEffect(2));
        addTrigger(Triggers.youCastThisSpell(), new ReplicateEffect("{U}"));
    }
}
