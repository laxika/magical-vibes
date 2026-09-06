package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "94")
public class DeadlyWanderings extends Card {

    public DeadlyWanderings() {
        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(
                        new AllOf(List.of(
                                new ControlsPermanentCount(1, new PermanentIsCreaturePredicate()),
                                new ControlsPermanentCountAtMost(1, new PermanentIsCreaturePredicate()))),
                        new StaticBoostEffect(2, 0,
                                Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK), GrantScope.OWN_CREATURES)));
    }
}
