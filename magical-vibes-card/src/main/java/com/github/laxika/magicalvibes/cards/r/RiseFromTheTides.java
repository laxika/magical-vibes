package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "82")
public class RiseFromTheTides extends Card {

    public RiseFromTheTides() {
        // Create a tapped 2/2 black Zombie creature token for each instant and sorcery card in your graveyard.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                new CardsInGraveyard(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )), CountScope.CONTROLLER),
                "Zombie", 2, 2,
                CardColor.BLACK, null,
                List.of(CardSubtype.ZOMBIE),
                Set.of(), Set.of(),
                false, true,
                Map.of(), List.of(),
                false, false, false, 0, Set.of()));
    }
}
