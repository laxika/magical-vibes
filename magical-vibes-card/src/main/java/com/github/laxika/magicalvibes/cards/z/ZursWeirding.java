package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayWithHandsRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.ZursWeirdingDrawReplacementEffect;

@CardRegistration(set = "9ED", collectorNumber = "114")
@CardRegistration(set = "8ED", collectorNumber = "116")
@CardRegistration(set = "6ED", collectorNumber = "108")
public class ZursWeirding extends Card {

    public ZursWeirding() {
        // Players play with their hands revealed.
        addEffect(EffectSlot.STATIC, new PlayWithHandsRevealedEffect());
        // If a player would draw a card, they reveal it instead. Then any other player may pay 2 life.
        // If a player does, put that card into its owner's graveyard. Otherwise, that player draws a card.
        addEffect(EffectSlot.STATIC, new ZursWeirdingDrawReplacementEffect());
    }
}
