package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "124")
public class UndeadServant extends Card {

    public UndeadServant() {
        // When this creature enters, create a 2/2 black Zombie creature token for each card named
        // Undead Servant in your graveyard.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                new CardsInGraveyard(new CardNamedPredicate("Undead Servant"), CountScope.CONTROLLER),
                "Zombie",
                2,
                2,
                CardColor.BLACK,
                List.of(CardSubtype.ZOMBIE),
                Set.of(),
                Set.of()
        ));
    }
}
