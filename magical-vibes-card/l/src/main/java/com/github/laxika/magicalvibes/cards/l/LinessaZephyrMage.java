package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "51")
public class LinessaZephyrMage extends Card {

    public LinessaZephyrMage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{U}{U}",
                List.of(ReturnToHandEffect.target()),
                "{X}{U}{U}, {T}: Return target creature with mana value X to its owner's hand.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentManaValueEqualsXPredicate())),
                        "Target must be a creature with mana value X"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(
                                new CardNamedPredicate("Linessa, Zephyr Mage"),
                                "Linessa, Zephyr Mage"
                        ),
                        new ReturnPermanentControlledByPlayerToHandEffect(
                                new PermanentIsCreaturePredicate(), "creature"),
                        new ReturnPermanentControlledByPlayerToHandEffect(
                                new PermanentIsArtifactPredicate(), "artifact"),
                        new ReturnPermanentControlledByPlayerToHandEffect(
                                new PermanentIsEnchantmentPredicate(), "enchantment"),
                        new ReturnPermanentControlledByPlayerToHandEffect(
                                new PermanentIsLandPredicate(), "land")
                ),
                "Grandeur — Discard another card named Linessa, Zephyr Mage: Target player returns a creature "
                        + "they control to its owner's hand, then repeats this process for an artifact, "
                        + "an enchantment, and a land.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));
    }
}
