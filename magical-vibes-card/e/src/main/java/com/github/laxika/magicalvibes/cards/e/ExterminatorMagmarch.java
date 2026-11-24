package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCouldTargetAnotherPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySinglePermanentOrPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

import static com.github.laxika.magicalvibes.model.effect.Subjects.controlledBy;
import static com.github.laxika.magicalvibes.model.effect.Subjects.controllerOf;
import static com.github.laxika.magicalvibes.model.effect.Subjects.eachOpponent;
import static com.github.laxika.magicalvibes.model.effect.Subjects.except;
import static com.github.laxika.magicalvibes.model.effect.Subjects.thisWay;
import static com.github.laxika.magicalvibes.model.effect.Subjects.triggeringSpellTargets;
import static com.github.laxika.magicalvibes.model.effect.Subjects.you;

@CardRegistration(set = "M3C", collectorNumber = "72")
@CardRegistration(set = "M3C", collectorNumber = "124")
public class ExterminatorMagmarch extends Card {

    public ExterminatorMagmarch() {
        PermanentPredicate nonlandPermanentAnOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        // The intervening "if" ("if another opponent controls one or more nonland permanents that spell could target")
        // is the last stack-entry filter; with no such permanent left as the ability resolves, none is chosen and nothing
        // is copied. "Another opponent" is one other than the controller of the permanent the spell targets.
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryTargetsOnlySinglePermanentOrPlayerPredicate(),
                                new StackEntryTargetsPermanentPredicate(nonlandPermanentAnOpponentControls),
                                new StackEntryCouldTargetAnotherPermanentPredicate(
                                        nonlandPermanentAnOpponentControls, true)))),
                SequenceEffect.of(
                        ChoosePermanentsEffect.exactly(1, controlledBy(
                                        except(eachOpponent(), controllerOf(triggeringSpellTargets())),
                                        new PermanentNotPredicate(new PermanentIsLandPredicate())),
                                "a permanent to copy the spell onto").thatTheTriggeringSpellCouldTarget(),
                        new CopyTriggeringSpellEffect().copiedBy(you()).targetingEach(thisWay(StepKind.CHOOSE))));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Exterminator Magmarch."
        ));
    }
}
