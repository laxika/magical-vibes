package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "110")
public class Torchling extends Card {

    public Torchling() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{R}: Untap Torchling."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new MustBlockSourceEffect(null)),
                "{R}: Target creature blocks Torchling this turn if able."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new ChangeTargetOfTargetSpellWithSingleTargetEffect()),
                "{R}: Change the target of target spell that targets only Torchling.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.CREATURE_SPELL,
                                        StackEntryType.ENCHANTMENT_SPELL,
                                        StackEntryType.SORCERY_SPELL,
                                        StackEntryType.INSTANT_SPELL,
                                        StackEntryType.ARTIFACT_SPELL,
                                        StackEntryType.PLANESWALKER_SPELL,
                                        StackEntryType.BATTLE_SPELL
                                )),
                                new StackEntryHasTargetPredicate(),
                                new StackEntryIsSingleTargetPredicate(),
                                new StackEntryTargetsSourcePredicate()
                        )),
                        "Target must be a spell with only Torchling as its target."
                )
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(1, -1)),
                "{1}: Torchling gets +1/-1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new BoostSelfEffect(-1, 1)),
                "{1}: Torchling gets -1/+1 until end of turn."
        ));
    }
}
