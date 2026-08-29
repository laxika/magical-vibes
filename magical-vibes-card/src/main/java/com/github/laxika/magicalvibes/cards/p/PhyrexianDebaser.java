package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "59")
public class PhyrexianDebaser extends Card {

    public PhyrexianDebaser() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(-2, -2)),
                "{T}, Sacrifice Phyrexian Debaser: Target creature gets -2/-2 until end of turn."
        ));
    }
}
