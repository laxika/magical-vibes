package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawLookAtTopReplacementEffect;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "291")
public class AladdinsLamp extends Card {

    public AladdinsLamp() {
        // {X}, {T}: The next time you would draw a card this turn, instead look at the top X cards of
        // your library, put all but one of them on the bottom of your library in a random order, then
        // draw a card. X can't be 0. Modeled as a one-shot delayed replacement of the next draw; the
        // paid X flows onto the stack entry's xValue, which the effect handler reads.
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new RegisterNextDrawLookAtTopReplacementEffect()),
                "{X}, {T}: The next time you would draw a card this turn, instead look at the top X cards "
                        + "of your library, put all but one of them on the bottom of your library in a "
                        + "random order, then draw a card. X can't be 0."));
    }
}
