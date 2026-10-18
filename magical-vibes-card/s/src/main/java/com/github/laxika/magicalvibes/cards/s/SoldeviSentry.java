package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "132a")
@CardRegistration(set = "ALL", collectorNumber = "132b")
public class SoldeviSentry extends Card {

    public SoldeviSentry() {
        // {1}: Choose target opponent. Regenerate this creature. When it regenerates this way, that
        // player may draw a card. The draw is owed only when the shield is actually spent, so it rides
        // on the shield rather than resolving here.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new RegenerateEffect(false, true)),
                "{1}: Choose target opponent. Regenerate this creature. When it regenerates this way, "
                        + "that player may draw a card.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")
        ));
    }
}
