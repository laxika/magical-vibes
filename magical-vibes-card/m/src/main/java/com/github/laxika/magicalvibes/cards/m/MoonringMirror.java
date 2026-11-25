package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "262")
public class MoonringMirror extends Card {

    public MoonringMirror() {
        // Whenever you draw a card, exile the top card of your library face down.
        // Nobody may look at the face-down cards (CR 406.3).
        addTrigger(Triggers.youDrawCard(), ExileTopCardsToSourceEffect.faceDownWithoutLookPermission(1));

        // At the beginning of your upkeep, you may exile all cards from your hand face down. If you
        // do, put all other cards you own exiled with this artifact into your hand. An empty hand
        // still counts as exiling all cards from it, so the return half happens either way.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                SequenceEffect.of(
                        ExileTargetPlayerHandEffect.handsOfWithSource(Subjects.you()).exiledFaceDown(),
                        new PutCardsIntoHandEffect(Subjects.except(
                                Subjects.exiledWithSource(Subjects.you()), Subjects.cardsThisWay(StepKind.EXILE)))),
                "Exile all cards from your hand face down and take back the cards exiled with Moonring Mirror?"));
    }
}
