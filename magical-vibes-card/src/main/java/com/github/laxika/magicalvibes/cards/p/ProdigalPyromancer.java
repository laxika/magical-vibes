package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "221")
public class ProdigalPyromancer extends Card {

    public ProdigalPyromancer() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(1)), true, "{T}: Prodigal Pyromancer deals 1 damage to any target."));
    }
}
