package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "111")
@CardRegistration(set = "SOS", collectorNumber = "331")
public class ChoreographedSparks extends Card {

    public ChoreographedSparks() {
        // "This spell can't be copied."
        setCantBeCopied(true);

        TargetFilter instantOrSorceryYouControl = new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                        new StackEntryControlledByPredicate()
                )),
                "Target must be an instant or sorcery spell you control."
        );
        TargetFilter creatureYouControl = new StackEntryPredicateTargetFilter(
                new StackEntryAllOfPredicate(List.of(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                        new StackEntryControlledByPredicate()
                )),
                "Target must be a creature spell you control."
        );

        // Copy an instant/sorcery spell — the copy allows choosing new targets (handled by CopySpellEffectHandler).
        // Copy a creature spell — the copy is a token that gains haste and is sacrificed at the next end step.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Copy target instant or sorcery spell you control. You may choose new targets for the copy",
                        new CopySpellEffect(),
                        instantOrSorceryYouControl
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Copy target creature spell you control. The copy gains haste and is sacrificed at the beginning of the end step",
                        new CopySpellEffect(null, true, true),
                        creatureYouControl
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Copy target instant or sorcery spell and target creature spell you control",
                        List.<CardEffect>of(new CopySpellEffect(), new CopySpellEffect(null, true, true)),
                        List.of(instantOrSorceryYouControl, creatureYouControl)
                )
        )));
    }
}
