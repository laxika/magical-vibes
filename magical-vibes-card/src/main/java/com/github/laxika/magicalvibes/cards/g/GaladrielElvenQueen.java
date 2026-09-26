package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnotherPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GaladrielElvenQueenEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LTC", collectorNumber = "3")
@CardRegistration(set = "LTC", collectorNumber = "83")
@CardRegistration(set = "LTC", collectorNumber = "88")
public class GaladrielElvenQueen extends Card {

    public GaladrielElvenQueen() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new ConditionalEffect(
                new AnotherPermanentEnteredThisTurn(new CardSubtypePredicate(CardSubtype.ELF)),
                new GaladrielElvenQueenEffect()));
    }
}
