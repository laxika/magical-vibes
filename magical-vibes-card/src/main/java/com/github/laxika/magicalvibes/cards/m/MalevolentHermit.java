package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BenevolentGeist;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DisturbCast;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MID", collectorNumber = "61")
public class MalevolentHermit extends Card {

    public MalevolentHermit() {
        setBackFaceCard(new BenevolentGeist());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new SacrificeSelfCost(), new CounterUnlessPaysEffect(3)),
                "{U}, Sacrifice Malevolent Hermit: Counter target noncreature spell unless its controller pays {3}.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryNotPredicate(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))),
                        "Target must be a noncreature spell."
                )
        ));

        addCastingOption(new DisturbCast("{2}{U}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "BenevolentGeist";
    }
}
