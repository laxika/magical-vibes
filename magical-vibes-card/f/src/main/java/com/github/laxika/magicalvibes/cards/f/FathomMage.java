package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "162")
@CardRegistration(set = "GK2", collectorNumber = "118")
@CardRegistration(set = "FIC", collectorNumber = "325")
@CardRegistration(set = "NCC", collectorNumber = "339")
@CardRegistration(set = "C16", collectorNumber = "198")
@CardRegistration(set = "CM2", collectorNumber = "156")
public class FathomMage extends Card {

    public FathomMage() {
        // Evolve is auto-loaded as Keyword.EVOLVE; the ally-creature entry scan drives it.

        // Whenever a +1/+1 counter is put on Fathom Mage, you may draw a card (once per counter).
        addTrigger(Triggers.plusOneCounterPutOnThis(), new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
