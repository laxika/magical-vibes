package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "63")
public class AugurOfSkulls extends Card {

    public AugurOfSkulls() {
        // "{1}{B}: Regenerate this creature."
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new RegenerateEffect()),
                "{1}{B}: Regenerate Augur of Skulls."
        ));

        // "Sacrifice this creature: Target player discards two cards. Activate only during your upkeep."
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER)),
                "Sacrifice Augur of Skulls: Target player discards two cards. Activate only during your upkeep.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                ),
                null,
                null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
