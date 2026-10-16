package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "KTK", collectorNumber = "43")
@CardRegistration(set = "M21", collectorNumber = "53")
@CardRegistration(set = "DDN", collectorNumber = "46")
@CardRegistration(set = "2X2", collectorNumber = "53")
public class JeskaiElder extends Card {

    public JeskaiElder() {
        // Prowess is a Scryfall-loaded keyword, so its boost is handled by the engine.
        // Whenever this creature deals combat damage to a player, you may draw a card. If you do,
        // discard a card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)
                ),
                "Draw a card, then discard a card?"
        ));
    }
}
