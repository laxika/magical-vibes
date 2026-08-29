package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "50")
public class ChangeTheEquation extends Card {

    public ChangeTheEquation() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target spell with mana value 2 or less",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryMaxManaValuePredicate(2),
                                "Target spell must have mana value 2 or less.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target red or green spell with mana value 6 or less",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryAllOfPredicate(List.of(
                                        new StackEntryColorInPredicate(Set.of(CardColor.RED, CardColor.GREEN)),
                                        new StackEntryMaxManaValuePredicate(6))),
                                "Target spell must be red or green with mana value 6 or less."))
        )));
    }
}
