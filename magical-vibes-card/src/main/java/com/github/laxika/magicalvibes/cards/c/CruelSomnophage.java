package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WOE", collectorNumber = "222")
public class CruelSomnophage extends Card {

    public CruelSomnophage() {
        setBackFaceCard(new CantWakeUp());
        addCastingOption(new AdventureCast("{1}{U}"));

        CardsInGraveyard creatureCardsInGraveyards =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(creatureCardsInGraveyards, creatureCardsInGraveyards));
    }

    @Override
    public String getBackFaceClassName() {
        return "CantWakeUp";
    }
}
