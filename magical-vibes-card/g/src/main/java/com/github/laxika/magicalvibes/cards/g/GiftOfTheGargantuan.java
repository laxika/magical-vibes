package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsRevealTwoTypesToHandThenRestEffect;

@CardRegistration(set = "ALA", collectorNumber = "132")
public class GiftOfTheGargantuan extends Card {

    public GiftOfTheGargantuan() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsRevealTwoTypesToHandThenRestEffect.creatureAndLandToHandRestOnBottom(4));
    }
}
