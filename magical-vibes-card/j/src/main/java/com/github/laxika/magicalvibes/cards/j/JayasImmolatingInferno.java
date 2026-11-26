package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "DOM", collectorNumber = "133")
@CardRegistration(set = "SLD", collectorNumber = "1063")
public class JayasImmolatingInferno extends Card {

    public JayasImmolatingInferno() {
        target(0, 3)
                .addSpellEffect(DealDamageEffect.toEachTarget(new XValue()));
    }
}
