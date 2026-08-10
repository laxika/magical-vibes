package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "78")
public class VolrathsDungeon extends Card {

    public VolrathsDungeon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(5), new SacrificeSelfEffect()),
                "Pay 5 life: Destroy this enchantment. Any player may activate this ability but only during their turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ).withActivatableByAnyPlayer());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DiscardCardTypeCost(null, null),
                        new TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect(1)),
                "Discard a card: Target player puts a card from their hand on top of their library. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
