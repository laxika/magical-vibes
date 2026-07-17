package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.SetControllerLifeToAmountEffect;

@CardRegistration(set = "ALA", collectorNumber = "14")
public class InvincibleHymn extends Card {

    public InvincibleHymn() {
        // Count the number of cards in your library. Your life total becomes that number.
        addEffect(EffectSlot.SPELL, new SetControllerLifeToAmountEffect(new CardsInLibrary(CountScope.CONTROLLER)));
    }
}
