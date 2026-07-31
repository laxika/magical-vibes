package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandFaceDownWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "120")
public class GusthasScepter extends Card {

    public GusthasScepter() {
        // {T}: Exile a card from your hand face down. You may look at it for as long as it remains
        // exiled. The face-down exile is only visible to this artifact's controller; the
        // toGraveyardOnControlLoss flag arms the control-loss clause below.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new ExileCardFromHandFaceDownWithSourceEffect(true)),
                "{T}: Exile a card from your hand face down. You may look at it for as long as it remains exiled."));

        // {T}: Return a card you own exiled with this artifact to your hand.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new PutCardExiledWithSourceIntoHandEffect()),
                "{T}: Return a card you own exiled with this artifact to your hand."));

        // When you lose control of this artifact, put all cards exiled with this artifact into
        // their owner's graveyard. Handled as an SBA-timed control watch
        // (StateBasedActionService.putExiledCardsIntoGraveyardOnControlLoss), which also fires when
        // the artifact leaves the battlefield.
    }
}
