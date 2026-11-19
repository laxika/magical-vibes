package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1802")
public class KarlachFuryOfAvernus extends Card {

    public KarlachFuryOfAvernus() {
        addTrigger(Triggers.youAttack(),
                TriggerOptions.DEFAULT.withInterveningIf(new FirstCombatPhase()),
                SequenceEffect.of(
                        new UntapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                        new PermanentIsAttackingPredicate())))),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE,
                                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                                new PermanentIsAttackingPredicate()),
                        new AdditionalCombatPhaseEffect(1)));
    }
}
