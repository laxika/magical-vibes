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

@CardRegistration(set = "C15", collectorNumber = "17")
public class CorpseAugur extends Card {

    public CorpseAugur() {
        // When this creature dies, you draw X cards and you lose X life, where X is the number
        // of creature cards in target player's graveyard.
        CardsInGraveyard creatureCards = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.TARGET_PLAYER);
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new DrawCardEffect(creatureCards),
                new LoseLifeEffect(creatureCards, LoseLifeRecipient.TARGET_PLAYER)));
    }
}
