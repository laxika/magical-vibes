package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MakeTargetUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.MaxPowerTargetFilter;

import java.util.List;

public class CraftyPathmage extends Card {

    public CraftyPathmage() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new MakeTargetUnblockableEffect()), true, "{T}: Target creature with power 2 or less can't be blocked this turn.", new MaxPowerTargetFilter(2)));
    }
}
