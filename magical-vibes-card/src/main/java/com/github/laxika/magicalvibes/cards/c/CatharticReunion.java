package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "KLD", collectorNumber = "109")
@CardRegistration(set = "IKO", collectorNumber = "110")
@CardRegistration(set = "2XM", collectorNumber = "121")
@CardRegistration(set = "KLR", collectorNumber = "116")
@CardRegistration(set = "SS3", collectorNumber = "2")
@CardRegistration(set = "TLE", collectorNumber = "164")
@CardRegistration(set = "ACR", collectorNumber = "94")
@CardRegistration(set = "SLZ", collectorNumber = "55")
@CardRegistration(set = "SLZ", collectorNumber = "176")
@CardRegistration(set = "SLZ", collectorNumber = "297")
@CardRegistration(set = "ECC", collectorNumber = "91")
public class CatharticReunion extends Card {

    public CatharticReunion() {
        // As an additional cost to cast this spell, discard two cards.
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null, 2));
        // Draw three cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
