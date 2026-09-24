package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "155")
@CardRegistration(set = "MSC", collectorNumber = "346")
public class BlackMarketConnections extends Card {

    public BlackMarketConnections() {
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Sell Contraband — Create a Treasure token. You lose 1 life.",
                        List.of(CreateTokenEffect.ofTreasureToken(1), new LoseLifeEffect(1))),
                new ChooseOneEffect.ChooseOneOption(
                        "Buy Information — Draw a card. You lose 2 life.",
                        List.of(new DrawCardEffect(), new LoseLifeEffect(2))),
                new ChooseOneEffect.ChooseOneOption(
                        "Hire a Mercenary — Create a 3/2 colorless Shapeshifter creature token with changeling. You lose 3 life.",
                        List.of(shapeshifterToken(), new LoseLifeEffect(3))))));
    }

    private static CreateTokenEffect shapeshifterToken() {
        return new CreateTokenEffect(
                "Shapeshifter", 3, 2, null,
                List.of(CardSubtype.SHAPESHIFTER), Set.of(Keyword.CHANGELING), Set.of());
    }
}
