package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SetOpponentMaximumHandSizeEffect;

@CardRegistration(set = "4ED", collectorNumber = "312")
public class CursedRack extends Card {

    public CursedRack() {
        // "As this artifact enters, choose an opponent. The chosen player's maximum hand size is four."
        // The chosen opponent is modeled as the controller's opponent(s) (matching Booby Trap's
        // single-opponent model); the static effect sets their maximum hand size to four.
        addEffect(EffectSlot.STATIC, new SetOpponentMaximumHandSizeEffect(4));
    }
}
