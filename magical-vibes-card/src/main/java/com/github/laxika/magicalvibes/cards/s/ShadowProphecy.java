package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "DMU", collectorNumber = "105")
public class ShadowProphecy extends Card {

    public ShadowProphecy() {
        // Domain — Look at the top X cards of your library, where X is the number of basic land
        // types among lands you control. Put up to two of them into your hand and the rest into
        // your graveyard.
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new BasicLandTypesAmongControlledLands(), new Fixed(2), null,
                LookDestination.GRAVEYARD, false));

        // You lose 2 life.
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2));
    }
}
