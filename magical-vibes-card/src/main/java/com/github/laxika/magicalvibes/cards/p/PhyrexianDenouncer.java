package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "61")
public class PhyrexianDenouncer extends Card {

    public PhyrexianDenouncer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(-1, -1)),
                "{T}, Sacrifice Phyrexian Denouncer: Target creature gets -1/-1 until end of turn."
        ));
    }
}
