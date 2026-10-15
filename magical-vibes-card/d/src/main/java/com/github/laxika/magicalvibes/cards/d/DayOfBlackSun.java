package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
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
        addEffect(EffectSlot.SPELL,
                new LosesAllAbilitiesEffect(GrantScope.ALL_CREATURES, AFFECTED_CREATURE,
                        EffectDuration.UNTIL_END_OF_TURN));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(AFFECTED_CREATURE));
    }
}
