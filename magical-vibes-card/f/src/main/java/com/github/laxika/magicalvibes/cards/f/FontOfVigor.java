package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "11")
public class FontOfVigor extends Card {

    public FontOfVigor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(7)),
                "Sacrifice Font of Vigor: You gain 7 life."
        ));
    }
}
