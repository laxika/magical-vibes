package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetPermanentUntilYourNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "199")
public class AbsorbingMan extends Card {

    public AbsorbingMan() {
        PermanentPredicate targetPredicate = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.AURA))
                )),
                new PermanentIsLandPredicate()
        ));
        target(new PermanentPredicateTargetFilter(
                targetPredicate,
                "Target must be an artifact, non-Aura enchantment, or land"), 0, 1)
                .addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                        new BecomeCopyOfTargetPermanentUntilYourNextTurnEffect(
                                "Absorbing Man", 4, 4,
                                Set.of(CardSubtype.HUMAN, CardSubtype.VILLAIN),
                                Set.of(CardType.CREATURE),
                                Set.of(CardSupertype.LEGENDARY),
                                Set.of(Keyword.VIGILANCE)));
    }
}
