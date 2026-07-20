package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnLandControlledByPlayerToHandEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "59")
public class KefnetTheMindful extends Card {

    public KefnetTheMindful() {
        // Flying, indestructible — auto-loaded from Scryfall.

        // Kefnet the Mindful can't attack or block unless you have seven or more cards in hand.
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new CardsInHandAtLeast(7),
                "you have seven or more cards in hand"
        ));

        // {3}{U}: Draw a card, then you may return a land you control to its owner's hand.
        // The land is chosen at resolution, after the draw, and is optional (MayEffect).
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(
                        new DrawCardEffect(1),
                        new MayEffect(
                                new ReturnLandControlledByPlayerToHandEffect(),
                                "You may return a land you control to its owner's hand."
                        )
                ),
                "{3}{U}: Draw a card, then you may return a land you control to its owner's hand."
        ));
    }
}
