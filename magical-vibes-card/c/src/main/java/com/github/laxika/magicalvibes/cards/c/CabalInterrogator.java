package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.RevealCardsChooseOneToDiscardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "57")
public class CabalInterrogator extends Card {

    public CabalInterrogator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{B}",
                List.of(new RevealCardsChooseOneToDiscardEffect(new XValue())),
                "{X}{B}, {T}: Target player reveals X cards from their hand and you choose one of them. "
                        + "That player discards that card. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withXValue());
    }
}
