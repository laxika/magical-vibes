package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "CMM", collectorNumber = "145")
@CardRegistration(set = "C15", collectorNumber = "17")
public class CorpseAugur extends Card {

    public CorpseAugur() {
        // When this creature dies, draw X cards and lose X life, where X is the number of
        // creature cards in target player's graveyard.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new DrawCardEffect(new CardsInGraveyard(
                        new CardTypePredicate(CardType.CREATURE), CountScope.TARGET_PLAYER)),
                new LoseLifeEffect(new CardsInGraveyard(
                        new CardTypePredicate(CardType.CREATURE), CountScope.TARGET_PLAYER),
                        LoseLifeRecipient.CONTROLLER)
        ));
    }
}
