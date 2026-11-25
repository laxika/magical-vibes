package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TMP", collectorNumber = "60")
public class Duplicity extends Card {

    public Duplicity() {
        // When this enchantment enters, exile the top five cards of your library face down.
        // toGraveyardOnControlLoss arms the control-loss watch that implements the last ability.
        // Nobody may look at the face-down cards (CR 406.3).
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTopCardsToSourceEffect(5, true, true,
                Subjects.you(), null, false, false));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new ReturnAllCardsExiledWithSourceToOwnerGraveyardEffect());

        // At the beginning of your upkeep, you may exile all cards from your hand face down. If you
        // do, put all other cards you own exiled with this enchantment into your hand. An empty hand
        // still counts as exiling all cards from it, so the return half happens either way.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        ExileTargetPlayerHandEffect.handsOfWithSource(Subjects.you()).exiledFaceDown(),
                        new PutCardsIntoHandEffect(Subjects.except(
                                Subjects.exiledWithSource(Subjects.you()), Subjects.cardsThisWay(StepKind.EXILE)))),
                "Exile all cards from your hand face down and take back the cards exiled with Duplicity?"));

        // At the beginning of your end step, discard a card.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new DiscardEffect(1, Subjects.you()));

    }
}
