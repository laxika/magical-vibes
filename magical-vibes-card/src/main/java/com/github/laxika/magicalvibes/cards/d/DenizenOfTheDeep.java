package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControllerOnlyTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ExcludeSelfTargetFilter;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "80")
public class DenizenOfTheDeep extends Card {

    public DenizenOfTheDeep() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnCreaturesToOwnersHandEffect(Set.of(new ControllerOnlyTargetFilter(), new ExcludeSelfTargetFilter())));
    }
}
