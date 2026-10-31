package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "145")
public class MindwhipSliver extends Card {

    public MindwhipSliver() {
        // All Slivers have "{2}, Sacrifice this permanent: Target player discards a card at random.
        // Activate only as a sorcery."
        ActivatedAbility discardAbility = new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true)),
                "{2}, Sacrifice this permanent: Target player discards a card at random. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        );
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

        addStaticEffect(new GrantActivatedAbilityEffect(
                discardAbility,
                GrantScope.ALL_PERMANENTS,
                sliver
        ));
        addStaticEffect(new GrantActivatedAbilityEffect(
                discardAbility,
                GrantScope.SELF,
                sliver
        ));
    }
}
