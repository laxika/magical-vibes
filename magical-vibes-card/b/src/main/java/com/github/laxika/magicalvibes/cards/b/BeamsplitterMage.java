package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
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

import static com.github.laxika.magicalvibes.model.effect.Subjects.controlledBy;
import static com.github.laxika.magicalvibes.model.effect.Subjects.except;
import static com.github.laxika.magicalvibes.model.effect.Subjects.source;
import static com.github.laxika.magicalvibes.model.effect.Subjects.thisWay;
import static com.github.laxika.magicalvibes.model.effect.Subjects.you;

@CardRegistration(set = "GRN", collectorNumber = "155")
public class BeamsplitterMage extends Card {

    public BeamsplitterMage() {
        // The intervening "if" ("if you control one or more other creatures that spell could target") is the last
        // stack-entry filter; with no such creature left as the ability resolves, none is chosen and nothing is copied.
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
                SequenceEffect.of(
                        ChoosePermanentsEffect.exactly(1, except(controlledBy(you(), new PermanentIsCreaturePredicate()),
                                source()), "another creature to copy the spell onto").thatTheTriggeringSpellCouldTarget(),
                        new CopyTriggeringSpellEffect().copiedBy(you()).targetingEach(thisWay(StepKind.CHOOSE))));
    }
}
