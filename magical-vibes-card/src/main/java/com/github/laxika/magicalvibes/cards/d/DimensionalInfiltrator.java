package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "44")
public class DimensionalInfiltrator extends Card {

    public DimensionalInfiltrator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{C}",
                List.of(new ExileTopCardOfTargetPlayerLibraryEffect(
                        0,
                        new MayEffect(ReturnToHandEffect.self(),
                                "Return Dimensional Infiltrator to its owner's hand?"))),
                "{1}{C}: Target opponent exiles the top card of their library. If it's a land card, you may return this creature to its owner's hand.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                )
        ));
    }
}
