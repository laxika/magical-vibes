package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "563")
public class MoonBoyDinoRider extends Card {

    public MoonBoyDinoRider() {
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.DINOSAUR), 1, CostModificationScope.SELF));

        addTrigger(Triggers.attacks(), TriggerOptions.DEFAULT.withEventCondition(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR))),
                BoostEffect.self(1, 1));
    }
}
