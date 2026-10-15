package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "3")
public class BanishingSlash extends Card {

    public BanishingSlash() {
        PermanentPredicate artifactOrEnchantmentOrTappedCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()))));

        target(new PermanentPredicateTargetFilter(
                artifactOrEnchantmentOrTappedCreature,
                "Target must be an artifact, enchantment, or tapped creature"), 0, 1)
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect(artifactOrEnchantmentOrTappedCreature));

        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AllOf(List.of(
                        new ControlsPermanent(new PermanentIsArtifactPredicate()),
                        new ControlsPermanent(new PermanentIsEnchantmentPredicate()))),
                new CreateTokenEffect(1, "Samurai", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.SAMURAI), Set.of(Keyword.VIGILANCE), Set.of())));
    }
}
