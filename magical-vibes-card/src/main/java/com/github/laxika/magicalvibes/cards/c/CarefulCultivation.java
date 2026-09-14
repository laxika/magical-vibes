package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "178")
public class CarefulCultivation extends Card {

    public CarefulCultivation() {
        var creature = new PermanentIsCreaturePredicate();
        var artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                creature));

        CreateTokenEffect humanMonk = new CreateTokenEffect(
                CardType.CREATURE, 1, "Human Monk", 1, 1, CardColor.GREEN, null,
                List.of(CardSubtype.HUMAN, CardSubtype.MONK), Set.of(), Set.of(), false, false,
                Map.of(), List.of(new ActivatedAbility(
                        true, null,
                        List.of(new AwardManaEffect(ManaColor.GREEN)),
                        "{T}: Add {G}."
                )), false, false, false, 0, Set.of());

        target(new PermanentPredicateTargetFilter(artifactOrCreature,
                "Target must be an artifact or creature"))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        creature,
                        new StaticBoostEffect(1, 3, GrantScope.ENCHANTED_CREATURE),
                        null))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        creature,
                        new GrantKeywordEffect(Keyword.REACH, GrantScope.ENCHANTED_CREATURE),
                        null))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        creature,
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(true, null,
                                        List.of(new AwardManaEffect(ManaColor.GREEN, 2)),
                                        "{T}: Add {G}{G}."),
                                GrantScope.ENCHANTED_CREATURE),
                        null));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(humanMonk),
                "Channel — {1}{G}, Discard this card: Create a 1/1 green Human Monk creature token "
                        + "with \"{T}: Add {G}.\""
        ));
    }
}
