package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "16")
public class MineBearer extends Card {

    public MineBearer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{T}, Sacrifice Mine Bearer: Destroy target attacking creature.",
                TargetFilters.attackingCreature()
        ));
    }
}
