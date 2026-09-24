package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AA2", collectorNumber = "19")
@CardRegistration(set = "MSC", collectorNumber = "197")
@CardRegistration(set = "MSC", collectorNumber = "430")
public class CurrencyConverter extends Card {

    public CurrencyConverter() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new MayEffect(
                new ExileDiscardedCardFromGraveyardEffect(true),
                "Exile that card from your graveyard?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{2}, {T}: Draw a card, then discard a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffect(
                        CreateTokenEffect.ofTreasureToken(1),
                        new CreateTokenEffect("Rogue", 2, 2, CardColor.BLACK,
                                List.of(CardSubtype.ROGUE), Set.of(), Set.of()))),
                "{T}: Put a card exiled with this artifact into its owner's graveyard. If it's a land card, create a Treasure token. If it's a nonland card, create a 2/2 black Rogue creature token."
        ));
    }
}
