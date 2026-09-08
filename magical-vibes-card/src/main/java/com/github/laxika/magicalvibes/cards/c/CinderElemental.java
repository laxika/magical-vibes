package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "87")
@CardRegistration(set = "MMQ", collectorNumber = "183")
public class CinderElemental extends Card {

    public CinderElemental() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(new XValue())),
                "{X}{R}, {T}, Sacrifice this creature: It deals X damage to any target."
        ));
    }
}
