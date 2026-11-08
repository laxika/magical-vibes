package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCouldTargetAnotherPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySinglePermanentOrPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "155")
public class BeamsplitterMage extends Card {

    public BeamsplitterMage() {
        // The intervening "if" ("if you control one or more other creatures that spell could target") is the last
        // stack-entry filter; the copy effect makes no copy when no such creature is left as it resolves.
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryTargetsOnlySinglePermanentOrPlayerPredicate(),
                                new StackEntryTargetsSourcePredicate(),
                                new StackEntryCouldTargetAnotherPermanentPredicate(new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentControlledBySourceControllerPredicate())), false)))),
                new CopySpellForEachOtherControlledCreatureEffect(true));
    }
}
