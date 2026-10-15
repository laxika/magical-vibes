package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "74")
public class KozileksTranslator extends Card {

    public KozileksTranslator() {
        // Pay 1 life: Add {C}. Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(1), new AwardManaEffect(ManaColor.COLORLESS)),
                "Pay 1 life: Add {C}. Activate only once each turn.",
                1));
    }
}
