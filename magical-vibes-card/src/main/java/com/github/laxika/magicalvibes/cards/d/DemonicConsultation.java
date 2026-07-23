package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNameExileTopRevealUntilNamedToHandEffect;

@CardRegistration(set = "ICE", collectorNumber = "121")
public class DemonicConsultation extends Card {

    public DemonicConsultation() {
        // Choose a card name. Exile the top six cards of your library, then reveal cards from the
        // top of your library until you reveal a card with the chosen name. Put that card into your
        // hand and exile all other cards revealed this way.
        addEffect(EffectSlot.SPELL, new ChooseNameExileTopRevealUntilNamedToHandEffect(6));
    }
}
