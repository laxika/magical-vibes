package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "67")
public class RevengeOfTheRats extends Card {

    public RevengeOfTheRats() {
        CardsInGraveyard creatureCards = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE,
                creatureCards,
                "Rat",
                1,
                1,
                CardColor.BLACK,
                null,
                List.of(CardSubtype.RAT),
                Set.of(),
                Set.of(),
                false,
                true,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of()));
        addCastingOption(new FlashbackCast("{2}{B}{B}"));
    }
}
