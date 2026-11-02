package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "86")
@CardRegistration(set = "WHO", collectorNumber = "691")
public class TheFlux extends Card {

    private static final PermanentPredicate OPPONENT_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
    ));

    public TheFlux() {
        addTrigger(Triggers.chapter(1), new DealDamageToTargetCreatureEffect(4, OPPONENT_CREATURE));
        setSagaChapterTargetFilter(1, Set.of(
                new PermanentPredicateTargetFilter(OPPONENT_CREATURE,
                        "Must target a creature an opponent controls")
        ));
        setSagaChapterTargetRequired(1);

        addTrigger(Triggers.chapter(2), new ExileTopCardMayPlayThisTurnEffect(false));
        addTrigger(Triggers.chapter(3), new ExileTopCardMayPlayThisTurnEffect(false));
        addTrigger(Triggers.chapter(4), new ExileTopCardMayPlayThisTurnEffect(false));
        addTrigger(Triggers.chapter(5), new ExileTopCardMayPlayThisTurnEffect(false));
        addTrigger(Triggers.chapter(6), new AwardManaEffect(ManaColor.RED, 6));
    }
}
