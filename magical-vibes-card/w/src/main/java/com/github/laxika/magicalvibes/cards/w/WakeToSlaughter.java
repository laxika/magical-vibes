package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MID", collectorNumber = "250")
@CardRegistration(set = "DBL", collectorNumber = "250")
public class WakeToSlaughter extends Card {

    public WakeToSlaughter() {
        addSpellEffect(ReturnTargetCardsFromGraveyardToHandEffect
                .opponentChoosesOneForHand(new CardTypePredicate(CardType.CREATURE)));
        addCastingOption(new FlashbackCast("{4}{B}{R}"));
    }
}
