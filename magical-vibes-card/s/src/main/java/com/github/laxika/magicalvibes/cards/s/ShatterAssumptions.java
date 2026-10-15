package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandAndDiscardMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "106")
public class ShatterAssumptions extends Card {

    public ShatterAssumptions() {
        PlayerPredicateTargetFilter opponentFilter = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent reveals their hand and discards all colorless nonland cards",
                        new RevealHandAndDiscardMatchingCardsEffect(new CardAllOfPredicate(List.of(
                                new CardIsColorlessPredicate(),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND))))),
                        opponentFilter),
                new ChooseOneEffect.ChooseOneOption(
                        "Target opponent reveals their hand and discards all multicolored cards",
                        new RevealHandAndDiscardMatchingCardsEffect(new CardIsMulticoloredPredicate()),
                        opponentFilter))));
    }
}
