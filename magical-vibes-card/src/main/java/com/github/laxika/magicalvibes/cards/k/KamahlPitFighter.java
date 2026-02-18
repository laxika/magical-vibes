package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

public class KamahlPitFighter extends Card {

    public KamahlPitFighter() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new DealDamageToAnyTargetEffect(3)), true, "{T}: Kamahl, Pit Fighter deals 3 damage to any target."));
    }
}
