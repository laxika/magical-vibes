package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "139")
public class TheLocustGod extends Card {

    public TheLocustGod() {
        // Flying — loaded from Scryfall

        // Whenever you draw a card, create a 1/1 blue and red Insect creature token with flying and haste.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new CreateTokenEffect(
                1, "Insect", 1, 1, CardColor.BLUE,
                Set.of(CardColor.BLUE, CardColor.RED),
                List.of(CardSubtype.INSECT),
                Set.of(Keyword.FLYING, Keyword.HASTE),
                Set.of()));

        // {2}{U}{R}: Draw a card, then discard a card.
        addActivatedAbility(new ActivatedAbility(
                false, "{2}{U}{R}",
                List.of(new DrawCardEffect(), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{2}{U}{R}: Draw a card, then discard a card."));

        // When The Locust God dies, return it to its owner's hand at the beginning of the next end step.
        addEffect(EffectSlot.ON_DEATH, new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));
    }
}
