package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "4")
public class FragmentReality extends Card {

    public FragmentReality() {
        var artifactCreatureOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentIsEnchantmentPredicate()));
        var targetPredicate = new PermanentAllOfPredicate(List.of(
                artifactCreatureOrEnchantment,
                new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        target(new PermanentPredicateTargetFilter(
                targetPredicate,
                "Target must be a nontoken artifact, creature, or enchantment an opponent controls"))
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentThenEffect(
                        EventStat.MANA_VALUE,
                        new SeekLibraryEffect(
                                new Fixed(1),
                                new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED,
                                new ManaValueBound(new EventValue(), false, -1)),
                        ThenEffectRecipient.TARGET_CONTROLLER));
    }
}
