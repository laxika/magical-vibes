package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "193")
public class JetmirNexusOfRevels extends Card {

    public JetmirNexusOfRevels() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(3, new PermanentIsCreaturePredicate()),
                new StaticBoostEffect(1, 0, Set.of(Keyword.VIGILANCE), GrantScope.ALL_OWN_CREATURES)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(6, new PermanentIsCreaturePredicate()),
                new StaticBoostEffect(1, 0, Set.of(Keyword.TRAMPLE), GrantScope.ALL_OWN_CREATURES)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanentCount(9, new PermanentIsCreaturePredicate()),
                new StaticBoostEffect(1, 0, Set.of(Keyword.DOUBLE_STRIKE), GrantScope.ALL_OWN_CREATURES)));
    }
}
