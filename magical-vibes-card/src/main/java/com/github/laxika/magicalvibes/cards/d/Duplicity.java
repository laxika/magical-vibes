package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileHandFaceDownThenReturnCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "TMP", collectorNumber = "60")
public class Duplicity extends Card {

    public Duplicity() {
        // When this enchantment enters, exile the top five cards of your library face down.
        // toGraveyardOnControlLoss arms the control-loss watch that implements the last ability.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileTopCardsToSourceEffect(5, true, true));

        // At the beginning of your upkeep, you may exile all cards from your hand face down. If you
        // do, put all other cards you own exiled with this enchantment into your hand. An empty hand
        // still counts as exiling all cards from it, so the return half happens either way.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new ExileHandFaceDownThenReturnCardsExiledWithSourceEffect(),
                "Exile all cards from your hand face down and take back the cards exiled with Duplicity?"));

        // At the beginning of your end step, discard a card.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new DiscardEffect(1, DiscardRecipient.CONTROLLER));

        // The entry effect also registers the control-loss trigger for cards it exiles.
    }
}
