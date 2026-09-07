package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "232")
public class SilumgarsCommand extends Card {

    public SilumgarsCommand() {
        setAllowSharedTargets(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target noncreature spell",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryNotPredicate(
                                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))),
                                "Target must be a noncreature spell.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Return target permanent to its owner's hand",
                        ReturnToHandEffect.target()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -3/-3 until end of turn",
                        new BoostTargetCreatureEffect(-3, -3),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target planeswalker",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsPlaneswalkerPredicate(),
                                "Target must be a planeswalker."))
        ), 2));
    }
}
