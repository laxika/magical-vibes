package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "216")
public class NecrogenSpellbomb extends Card {

    public NecrogenSpellbomb() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new SacrificeSelfCost(), new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)),
                "{B}, Sacrifice Necrogen Spellbomb: Target player discards a card.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}, Sacrifice Necrogen Spellbomb: Draw a card."
        ));
    }
}
