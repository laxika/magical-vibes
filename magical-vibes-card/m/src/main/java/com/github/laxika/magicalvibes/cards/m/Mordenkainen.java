package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeHandAndLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeDuration;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "64")
public class Mordenkainen extends Card {

    public Mordenkainen() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(
                        2, 1, HandToLibraryPlacement.BOTTOM)),
                "+2: Draw two cards, then put a card from your hand on the bottom of your library."
        ));

        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        Scaled tokenSize = new Scaled(cardsInHand, 2);
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new CreateTokenEffect(
                        1, "Dog Illusion", 0, 0,
                        CardColor.BLUE, List.of(CardSubtype.DOG, CardSubtype.ILLUSION),
                        Set.of(), Set.of(),
                        Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(tokenSize, tokenSize))
                )),
                "\u22122: Create a blue Dog Illusion creature token with \"This token's power and toughness are each equal to twice the number of cards in your hand.\""
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(
                        new ExchangeHandAndLibraryEffect(),
                        new ShuffleLibraryEffect(false),
                        new GrantNoMaximumHandSizeEffect(NoMaximumHandSizeDuration.REST_OF_GAME)
                ),
                "\u221210: Exchange your hand and library, then shuffle. You get an emblem with \"You have no maximum hand size.\""
        ));
    }
}
