package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "ISD", collectorNumber = "139")
@CardRegistration(set = "EMA", collectorNumber = "125")
@CardRegistration(set = "C15", collectorNumber = "149")
@CardRegistration(set = "C19", collectorNumber = "137")
public class DesperateRavings extends Card {

    public DesperateRavings() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.CONTROLLER, true));
        addCastingOption(new FlashbackCast("{2}{U}"));
    }
}
