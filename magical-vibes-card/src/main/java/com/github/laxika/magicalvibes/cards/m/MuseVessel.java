package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardExiledWithSourceMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "163")
public class MuseVessel extends Card {

    public MuseVessel() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new TargetPlayerExilesFromHandEffect(1)),
                "{3}, {T}: Target player exiles a card from their hand. Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ChooseCardExiledWithSourceMayPlayThisTurnEffect()),
                "{1}: Choose a card exiled with this artifact. You may play that card this turn."
        ));
    }
}
