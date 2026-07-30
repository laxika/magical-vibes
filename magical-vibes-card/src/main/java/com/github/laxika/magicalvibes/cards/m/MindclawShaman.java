package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayCastFromTargetPlayerHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTargetHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "142")
public class MindclawShaman extends Card {

    public MindclawShaman() {
        // When this creature enters, target opponent reveals their hand. You may cast an instant
        // or sorcery spell from among those cards without paying its mana cost.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTargetHandEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayCastFromTargetPlayerHandWithoutPayingManaCostEffect(
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY)))));
    }
}
