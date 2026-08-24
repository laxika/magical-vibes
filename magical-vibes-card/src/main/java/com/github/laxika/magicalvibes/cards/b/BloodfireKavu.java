package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "58")
public class BloodfireKavu extends Card {

    public BloodfireKavu() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new MassDamageEffect(2)),
                "{R}, Sacrifice Bloodfire Kavu: It deals 2 damage to each creature."
        ));
    }
}
