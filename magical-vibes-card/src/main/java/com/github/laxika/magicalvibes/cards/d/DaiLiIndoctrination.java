package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "93")
public class DaiLiIndoctrination extends Card {

    public DaiLiIndoctrination() {
        var opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent reveals their hand. You choose a nonland permanent card from it. That player discards that card.",
                        new ChooseCardsFromTargetHandEffect(
                                1, List.of(CardType.LAND), new CardIsPermanentPredicate(),
                                HandChoiceDestination.DISCARD),
                        opponentFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Earthbend 2",
                        new EarthbendTargetLandEffect(2)
                ))));
    }
}
