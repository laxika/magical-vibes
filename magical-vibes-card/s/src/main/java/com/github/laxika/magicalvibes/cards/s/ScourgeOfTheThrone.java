package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.condition.SourceMatchesPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttacksPlayerWithMostLifePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "VMA", collectorNumber = "184")
@CardRegistration(set = "SLD", collectorNumber = "1033")
@CardRegistration(set = "CMM", collectorNumber = "253")
@CardRegistration(set = "CMM", collectorNumber = "549")
@CardRegistration(set = "LTC", collectorNumber = "225")
@CardRegistration(set = "TDC", collectorNumber = "230")
@CardRegistration(set = "MKC", collectorNumber = "160")
@CardRegistration(set = "FDC", collectorNumber = "173")
public class ScourgeOfTheThrone extends Card {

    public ScourgeOfTheThrone() {
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new SourceMatchesPermanentPredicate(new PermanentAttacksPlayerWithMostLifePredicate()))
                        .withLimit(TriggerLimit.FIRST_TIME_EACH_TURN),
                SequenceEffect.of(
                        new UntapPermanentsEffect(
                                Subjects.controlledBy(Subjects.eachPlayer(),
                                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                                new PermanentIsAttackingPredicate())))),
                        new AdditionalCombatPhaseEffect(1)));
    }
}
