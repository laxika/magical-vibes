package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "SUM", collectorNumber = "274")
@CardRegistration(set = "3ED", collectorNumber = "274")
@CardRegistration(set = "V10", collectorNumber = "12")
@CardRegistration(set = "VMA", collectorNumber = "283")
@CardRegistration(set = "MPS", collectorNumber = "24")
public class SolRing extends Card {

    public SolRing() {
        // {T}: Add {C}{C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 2)),
                "{T}: Add {C}{C}."
        ));
    }
}
