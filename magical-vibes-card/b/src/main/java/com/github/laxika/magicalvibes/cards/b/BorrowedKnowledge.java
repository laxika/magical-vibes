package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEqualToTargetPlayerHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "178")
public class BorrowedKnowledge extends Card {

    public BorrowedKnowledge() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Discard your hand, then draw cards equal to the number of cards in target opponent's hand",
                        new DiscardOwnHandThenDrawEqualToTargetPlayerHandSizeEffect(),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent"
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Discard your hand, then draw cards equal to the number of cards discarded this way",
                        new DiscardOwnHandThenDrawThatManyEffect()
                )
        )));
    }
}
