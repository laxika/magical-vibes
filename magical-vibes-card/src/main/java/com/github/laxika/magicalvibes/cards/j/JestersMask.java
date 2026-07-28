package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPlayerHandOnLibraryThenSearchThatManyToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "325")
public class JestersMask extends Card {

    public JestersMask() {
        // This artifact enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {1}, {T}, Sacrifice this artifact: Target opponent puts the cards from their hand on top of
        // their library. Search that player's library for that many cards. That player puts those
        // cards into their hand, then shuffles.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new PutTargetPlayerHandOnLibraryThenSearchThatManyToHandEffect()),
                "{1}, {T}, Sacrifice Jester's Mask: Target opponent puts the cards from their hand on top of "
                        + "their library. Search that player's library for that many cards. That player puts "
                        + "those cards into their hand, then shuffles.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
