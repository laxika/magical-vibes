package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "156")
public class PeerlessSamurai extends Card {

    public PeerlessSamurai() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.SAMURAI),
                                new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)))),
                TriggerOptions.DEFAULT.withInterveningIf(new AttacksAlone()),
                new ReduceCastCostForNextMatchingSpellEffect(new CardTruePredicate(), 1));
    }
}
