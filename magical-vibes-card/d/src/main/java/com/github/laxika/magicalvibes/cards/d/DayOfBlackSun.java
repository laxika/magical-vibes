package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "94")
public class DayOfBlackSun extends Card {

    private static final PermanentPredicate AFFECTED_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentManaValueAtMostXPredicate()
    ));

    public DayOfBlackSun() {
        addSpellEffect(new LosesAllAbilitiesEffect(Subjects.otherCreatures(), AFFECTED_CREATURE,
                        Duration.UNTIL_END_OF_TURN));
        addSpellEffect(DestroyTargetPermanentEffect.all(AFFECTED_CREATURE));
    }
}
