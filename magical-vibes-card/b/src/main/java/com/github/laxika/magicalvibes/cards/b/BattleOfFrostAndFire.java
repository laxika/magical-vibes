package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "KHM", collectorNumber = "204")
public class BattleOfFrostAndFire extends Card {

    public BattleOfFrostAndFire() {
        addTrigger(Triggers.chapter(1), DealDamageEffect.toEach(Subjects.controlledBy(Subjects.eachPlayer(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.GIANT)))),
                        new PermanentIsPlaneswalkerPredicate()))), 4));
        addTrigger(Triggers.chapter(2), new ScryEffect(3));
        addTrigger(Triggers.chapter(3),
                RegisterDelayedControllerSpellCastTriggerEffect.thisTurn(null, List.of(
                                new DrawCardEffect(2),
                                new DiscardEffect(1, Subjects.you())))
                .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryMaxManaValuePredicate(4))));
    }
}
