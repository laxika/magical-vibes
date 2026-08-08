package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "BOK", collectorNumber = "109")
public class IreOfKaminari extends Card {

    public IreOfKaminari() {
        // Ire of Kaminari deals damage to any target equal to the number of Arcane cards in your graveyard.
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(
                new CardsInGraveyard(new CardSubtypePredicate(CardSubtype.ARCANE), CountScope.CONTROLLER)));
    }
}
