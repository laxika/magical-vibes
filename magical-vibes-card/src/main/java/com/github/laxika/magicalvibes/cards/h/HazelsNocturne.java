package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "BLB", collectorNumber = "97")
public class HazelsNocturne extends Card {

    public HazelsNocturne() {
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardTypePredicate(CardType.CREATURE), 2));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
