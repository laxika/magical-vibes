package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SylvanLibraryDrawEffect;

@CardRegistration(set = "5ED", collectorNumber = "329")
@CardRegistration(set = "4ED", collectorNumber = "273")
public class SylvanLibrary extends Card {

    public SylvanLibrary() {
        // At the beginning of your draw step, you may draw two additional cards. If you do, choose two
        // cards in your hand drawn this turn. For each of those cards, pay 4 life or put the card on
        // top of your library.
        addEffect(EffectSlot.DRAW_TRIGGERED, new MayEffect(
                new SylvanLibraryDrawEffect(),
                "Draw two additional cards?"
        ));
    }
}
