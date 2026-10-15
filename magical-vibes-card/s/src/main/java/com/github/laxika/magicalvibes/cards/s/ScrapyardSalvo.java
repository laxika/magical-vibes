package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "NPH", collectorNumber = "94")
public class ScrapyardSalvo extends Card {

    public ScrapyardSalvo() {
        // Deals damage to target player equal to the number of artifact cards in YOUR graveyard.
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(new CardsInGraveyard(
                new CardTypePredicate(CardType.ARTIFACT), CountScope.CONTROLLER), DamageRecipient.TARGET_PLAYER));
    }
}
