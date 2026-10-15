package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawFromExiledPileReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileFaceDownPileEffect;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "309")
public class MangarasTome extends Card {

    public MangarasTome() {
        // When this artifact enters, search your library for five cards, exile them in a face-down
        // pile, and shuffle that pile. Then shuffle your library.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryForCardsToExileFaceDownPileEffect(5));
        // {2}: The next time you would draw a card this turn, instead put the top card of the exiled
        // pile into its owner's hand. Modeled as a one-shot delayed replacement of the next draw.
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new RegisterNextDrawFromExiledPileReplacementEffect()),
                "{2}: The next time you would draw a card this turn, instead put the top card of the "
                        + "exiled pile into its owner's hand."));
    }
}
