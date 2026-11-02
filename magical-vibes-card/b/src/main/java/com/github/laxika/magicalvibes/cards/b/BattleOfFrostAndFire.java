package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
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

@CardRegistration(set = "KHM", collectorNumber = "204")
public class BattleOfFrostAndFire extends Card {

    public BattleOfFrostAndFire() {
        addTrigger(Triggers.chapter(1), new MassDamageEffect(
                4,
                false,
                false,
                true,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.GIANT)),
                        new PermanentIsPlaneswalkerPredicate()))));
        addTrigger(Triggers.chapter(2), new ScryEffect(3));
        addTrigger(Triggers.chapter(3),
                RegisterDelayedControllerSpellCastTriggerEffect.withStackEntryFilter(
                        new StackEntryNotPredicate(new StackEntryMaxManaValuePredicate(4)),
                        List.of(
                                new DrawCardEffect(2),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                        false));
    }
}
