package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySingleCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "201")
public class IvyGleefulSpellthief extends Card {

    public IvyGleefulSpellthief() {
        addTrigger(Triggers.playerCastsSpell()
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryTargetsOnlySingleCreaturePredicate(),
                                new StackEntryNotPredicate(new StackEntryTargetsSourcePredicate())
                        ))),
                new MayEffect(CopyTriggeringSpellEffect.targetingSource(), "Copy that spell?"));
    }
}
