package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "M3C", collectorNumber = "57")
@CardRegistration(set = "M3C", collectorNumber = "109")
public class HideousTaskmaster extends Card {

    public HideousTaskmaster() {
        // Annihilator 1 (printed).
        addTrigger(Triggers.attacks(), new SacrificePermanentsEffect(
                1, new PermanentTruePredicate(), Subjects.defendingPlayer()));
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.creatureAnOpponentControls(), 0, 99)
                .addTrigger(Triggers.youCastThisSpell(),
                        new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN),
                        new UntapPermanentsEffect(Subjects.allTargets()),
                        new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.HASTE), Subjects.target()),
                        new GrantEffectToTargetUntilEndOfTurnEffect(
                                new TriggeredAbility(Triggers.attacks(),
                                new SacrificePermanentsEffect(
                                        1, new PermanentTruePredicate(), Subjects.defendingPlayer()))));
    }
}
