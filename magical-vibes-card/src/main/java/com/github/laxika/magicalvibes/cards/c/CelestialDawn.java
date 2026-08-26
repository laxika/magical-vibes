package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledNonlandPermanentsAreColorEffect;
import com.github.laxika.magicalvibes.model.effect.SpendWhiteManaAsAnyColorEffect;

@CardRegistration(set = "MIR", collectorNumber = "6")
@CardRegistration(set = "6ED", collectorNumber = "7")
@CardRegistration(set = "TSB", collectorNumber = "3")
public class CelestialDawn extends Card {

    public CelestialDawn() {
        // Lands you control are Plains.
        addEffect(EffectSlot.STATIC, new ControlledLandsBecomeTypeEffect(CardSubtype.PLAINS));

        // Nonland permanents you control are white. (The clause about spells you control and nonland
        // cards you own outside the battlefield has no observable effect in this engine — colour is
        // only read for objects on the battlefield — so it is covered by the permanent-side setter.)
        addEffect(EffectSlot.STATIC, new ControlledNonlandPermanentsAreColorEffect(CardColor.WHITE));

        // You may spend white mana as though it were mana of any color. You may spend other mana
        // only as though it were colorless mana.
        addEffect(EffectSlot.STATIC, new SpendWhiteManaAsAnyColorEffect());
    }
}
