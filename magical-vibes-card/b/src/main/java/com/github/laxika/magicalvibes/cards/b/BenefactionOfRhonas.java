package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;

@CardRegistration(set = "AKH", collectorNumber = "156")
public class BenefactionOfRhonas extends Card {

    public BenefactionOfRhonas() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsRevealTwoTypesToHandThenRestEffect.creatureAndEnchantmentToHandRestToGraveyard(5));
    }
}
