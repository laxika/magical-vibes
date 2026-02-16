package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

public class HonorGuard extends Card {

    public HonorGuard() {
        addActivatedAbility(new ActivatedAbility(false, "{W}", List.of(new BoostSelfEffect(0, 1)), false, "{W}: Honor Guard gets +0/+1 until end of turn."));
    }
}
