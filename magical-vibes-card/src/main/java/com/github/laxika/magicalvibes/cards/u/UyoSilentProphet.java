package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "99")
public class UyoSilentProphet extends Card {

    public UyoSilentProphet() {
        // {2}, Return two lands you control to their owner's hand: Copy target instant or sorcery
        // spell. You may choose new targets for the copy (the retarget prompt is offered by
        // CopySpellEffectHandler).
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new ReturnMultiplePermanentsToHandCost(2, new PermanentIsLandPredicate()),
                        new CopySpellEffect()),
                "{2}, Return two lands you control to their owner's hand: Copy target instant or sorcery spell. You may choose new targets for the copy.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        "Target must be an instant or sorcery spell.")));
    }
}
