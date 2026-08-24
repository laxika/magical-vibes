package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "205")
public class MerfolkSkydiver extends Card {

    public MerfolkSkydiver() {
        target(TargetFilters.creatureYouControl()).addEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)
        );
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{U}",
                List.of(new ProliferateEffect()),
                "{3}{G}{U}: Proliferate."
        ));
    }
}
