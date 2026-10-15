package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileHandFaceDownThenReturnCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "CHK", collectorNumber = "262")
public class MoonringMirror extends Card {

    public MoonringMirror() {
        // Whenever you draw a card, exile the top card of your library face down.
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new ExileTopCardsToSourceEffect(1, true));

        // At the beginning of your upkeep, you may exile all cards from your hand face down. If you
        // do, put all other cards you own exiled with this artifact into your hand. An empty hand
        // still counts as exiling all cards from it, so the return half happens either way.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                new ExileHandFaceDownThenReturnCardsExiledWithSourceEffect(),
                "Exile all cards from your hand face down and take back the cards exiled with Moonring Mirror?"));
    }
}
