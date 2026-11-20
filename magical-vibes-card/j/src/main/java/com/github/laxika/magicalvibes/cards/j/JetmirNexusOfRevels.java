package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "193")
@CardRegistration(set = "SLD", collectorNumber = "1509")
@CardRegistration(set = "SLD", collectorNumber = "1555")
public class JetmirNexusOfRevels extends Card {

    public JetmirNexusOfRevels() {
        addStaticEffect(new ConditionalEffect(
                new ControlsPermanentCount(3, new PermanentIsCreaturePredicate()),
                new StaticBoostEffect(1, 0, Set.of(Keyword.VIGILANCE), Subjects.creaturesYouControl())));
        addStaticEffect(new ConditionalEffect(
                new ControlsPermanentCount(6, new PermanentIsCreaturePredicate()),
                new StaticBoostEffect(1, 0, Set.of(Keyword.TRAMPLE), Subjects.creaturesYouControl())));
        addStaticEffect(new ConditionalEffect(
                new ControlsPermanentCount(9, new PermanentIsCreaturePredicate()),
                new StaticBoostEffect(1, 0, Set.of(Keyword.DOUBLE_STRIKE), Subjects.creaturesYouControl())));
    }
}
