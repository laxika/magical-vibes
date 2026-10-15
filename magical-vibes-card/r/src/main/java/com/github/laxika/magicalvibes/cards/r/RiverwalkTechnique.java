package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "54")
public class RiverwalkTechnique extends Card {

    public RiverwalkTechnique() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "The owner of target nonland permanent puts it on their choice of the top or bottom of their library",
                        new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0),
                        TargetFilters.nonlandPermanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Counter target noncreature spell",
                        new CounterSpellEffect(),
                        new StackEntryPredicateTargetFilter(
                                new StackEntryNotPredicate(
                                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))),
                                "Target must be a noncreature spell."))
        )));
    }
}
