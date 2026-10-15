package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "21")
public class MagesAttendant extends Card {

    public MagesAttendant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                CardType.CREATURE, 1, "Wizard", 1, 1,
                CardColor.BLUE, null, List.of(CardSubtype.WIZARD), Set.of(), Set.of(),
                false, false, Map.of(), List.of(new ActivatedAbility(
                        false,
                        "{1}",
                        List.of(new SacrificeSelfCost(), new CounterUnlessPaysEffect(1)),
                        "{1}, Sacrifice this token: Counter target noncreature spell unless its controller pays {1}.",
                        new StackEntryPredicateTargetFilter(
                                new StackEntryNotPredicate(
                                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))),
                                "Target must be a noncreature spell."
                        )
                )), false, false, false, 0, Set.of()));
    }
}
