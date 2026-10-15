package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "175")
public class FlameElemental extends Card {

    public FlameElemental() {
        // {R}, {T}, Sacrifice Flame Elemental: It deals damage equal to its power to target creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new SacrificeSelfCost(), new DealDamageToTargetCreatureEffect(new SourcePower())),
                "{R}, {T}, Sacrifice Flame Elemental: It deals damage equal to its power to target creature."
        ));
    }
}
