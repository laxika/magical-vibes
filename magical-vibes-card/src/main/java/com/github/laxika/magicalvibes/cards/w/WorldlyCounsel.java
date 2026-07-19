package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "CON", collectorNumber = "39")
public class WorldlyCounsel extends Card {

    public WorldlyCounsel() {
        // Domain — Look at the top X cards of your library, where X is the number of basic land
        // types among lands you control. Put one of those cards into your hand and the rest on the
        // bottom of your library in any order.
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseOneToHandRestOnBottom(
                new BasicLandTypesAmongControlledLands()));
    }
}
