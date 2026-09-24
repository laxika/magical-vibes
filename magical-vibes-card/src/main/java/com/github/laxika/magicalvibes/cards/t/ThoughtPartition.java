package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetChosenCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "4")
public class ThoughtPartition extends Card {

    public ThoughtPartition() {
        ChooseCardsFromTargetHandEffect choose = new ChooseCardsFromTargetHandEffect(
                new Fixed(1), List.of(CardType.LAND), List.of(), HandChoiceDestination.KEEP_IN_HAND,
                false, null, 0, true, false, false, true, false, false, 0
        ).withChosenCardThen(null,
                new PerpetuallySetChosenCardCharacteristicsEffect(CardColor.WHITE, "{5}"));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, choose);
    }
}
