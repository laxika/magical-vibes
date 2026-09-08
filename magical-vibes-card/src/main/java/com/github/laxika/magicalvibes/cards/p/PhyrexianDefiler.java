package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "60")
public class PhyrexianDefiler extends Card {

    public PhyrexianDefiler() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(-3, -3)),
                "{T}, Sacrifice Phyrexian Defiler: Target creature gets -3/-3 until end of turn."
        ));
    }
}
