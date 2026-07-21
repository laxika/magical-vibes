package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "165")
public class MirageMirror extends Card {

    public MirageMirror() {
        // {2}: This artifact becomes a copy of target artifact, creature, enchantment, or land until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect()),
                "{2}: This artifact becomes a copy of target artifact, creature, enchantment, or land until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsEnchantmentPredicate(),
                                new PermanentIsLandPredicate()
                        )),
                        "Target must be an artifact, creature, enchantment, or land"
                )
        ));
    }
}
