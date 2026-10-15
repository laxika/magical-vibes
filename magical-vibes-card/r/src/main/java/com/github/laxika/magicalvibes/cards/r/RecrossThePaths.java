package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandToBattlefieldRestToBottomEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "133")
public class RecrossThePaths extends Card {

    public RecrossThePaths() {
        // Reveal cards from the top of your library until you reveal a land card. Put that card onto
        // the battlefield and the rest on the bottom of your library in any order. (pre-clash body)
        // Then clash with an opponent. If you win, return Recross the Paths to its owner's hand.
        addEffect(EffectSlot.SPELL, new ClashEffect(
                List.<CardEffect>of(new RevealUntilLandToBattlefieldRestToBottomEffect()),
                ReturnToHandEffect.selfSpell(),
                false));
    }
}
