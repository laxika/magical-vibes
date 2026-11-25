package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

@CardRegistration(set = "GTC", collectorNumber = "52")
public class SpellRupture extends Card {

    public SpellRupture() {
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(new GreatestPowerAmongControlled()));
    }
}
