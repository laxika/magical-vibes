package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileHandFaceDownThenReturnCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "60")
public class Duplicity extends Card {

    public Duplicity() {
        // When this enchantment enters, exile the top five cards of your library face down.
        // toGraveyardOnControlLoss arms the control-loss watch that implements the last ability.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTopCardsToSourceEffect(5, true, true));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect());

        // At the beginning of your upkeep, you may exile all cards from your hand face down. If you
        // do, put all other cards you own exiled with this enchantment into your hand. An empty hand
        // still counts as exiling all cards from it, so the return half happens either way.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new ExileHandFaceDownThenReturnCardsExiledWithSourceEffect(),
                "Exile all cards from your hand face down and take back the cards exiled with Duplicity?"));

        // At the beginning of your end step, discard a card.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new DiscardEffect(1, DiscardRecipient.CONTROLLER));

    }
}
