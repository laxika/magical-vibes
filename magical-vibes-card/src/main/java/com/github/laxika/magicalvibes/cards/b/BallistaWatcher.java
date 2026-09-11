package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "143")
public class BallistaWatcher extends Card {

    public BallistaWatcher() {
        setBackFaceCard(new BallistaWielder());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new DealDamageToAnyTargetEffect(1)),
                "{2}{R}, {T}: This creature deals 1 damage to any target."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "BallistaWielder";
    }
}
