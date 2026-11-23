package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "131")
public class HaphazardBombardment extends Card {

    public HaphazardBombardment() {
        // "When this enchantment enters, choose four nonenchantment permanents you don't
        // control and put an aim counter on each of them."
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ChoosePermanentsEffect.exactly(4,
                        Subjects.controlledBy(Subjects.eachOpponent(),
                                new PermanentNotPredicate(new PermanentIsEnchantmentPredicate())),
                        "four nonenchantment permanents you don't control"),
                new PutCounterOnTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE), CounterType.AIM));

        // "At the beginning of your end step, if two or more permanents you don't control
        // have an aim counter on them, destroy one of those permanents at random."
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new AnyPlayerControlsPermanentCount(2,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasCountersPredicate(CounterType.AIM),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))))),
                // Among those without indestructible (its ruling, 2018-04-27).
                ChoosePermanentsEffect.atRandom(1, Subjects.controlledBy(Subjects.eachOpponent(),
                                new PermanentAllOfPredicate(List.of(new PermanentHasCountersPredicate(CounterType.AIM),
                                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.INDESTRUCTIBLE))))),
                        "one of those permanents"),
                new DestroyTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE)));
    }
}
