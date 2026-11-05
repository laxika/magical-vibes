package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "131")
public class AkkiRonin extends Card {

    public AkkiRonin() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.SAMURAI),
                                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)))),
                TriggerOptions.DEFAULT.withEventCondition(new AttacksAlone()),
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
