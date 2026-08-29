package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "292")
public class CrystalChimes extends Card {

    public CrystalChimes() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new SacrificeSelfCost(),
                        new ReturnCardsFromControllerGraveyardToHandEffect(
                                new CardTypePredicate(CardType.ENCHANTMENT), new Fixed(Integer.MAX_VALUE))),
                "{3}, {T}, Sacrifice this artifact: Return all enchantment cards from your graveyard to your hand."
        ));
    }
}
