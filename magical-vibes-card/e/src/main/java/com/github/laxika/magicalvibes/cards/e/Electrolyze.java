package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "GPT", collectorNumber = "111")
@CardRegistration(set = "MM2", collectorNumber = "174")
@CardRegistration(set = "MMA", collectorNumber = "175")
@CardRegistration(set = "GK1", collectorNumber = "37")
@CardRegistration(set = "IMA", collectorNumber = "198")
@CardRegistration(set = "STA", collectorNumber = "60")
@CardRegistration(set = "CMD", collectorNumber = "197")
public class Electrolyze extends Card {

    public Electrolyze() {
        // Electrolyze deals 2 damage divided as you choose among one or two targets.
        target(1, 2).addSpellEffect(DealDividedDamageEffect.chosenAmongAnyTargets(2));
        addSpellEffect(new DrawCardEffect(1));
    }
}
