package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FRF", collectorNumber = "139")
public class SuddenReclamation extends Card {

    public SuddenReclamation() {
        addEffect(EffectSlot.SPELL, new MillEffect(4, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToHandEffect(
                new CardTypePredicate(CardType.CREATURE), new Fixed(1), false));
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToHandEffect(
                new CardTypePredicate(CardType.LAND), new Fixed(1), false));
    }
}
