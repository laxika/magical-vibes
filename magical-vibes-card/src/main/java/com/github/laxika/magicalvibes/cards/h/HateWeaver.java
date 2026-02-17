package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CreatureColorTargetFilter;

import java.util.List;
import java.util.Set;

public class HateWeaver extends Card {

    public HateWeaver() {
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new BoostTargetCreatureEffect(1, 0)), true, "{2}: Target blue or red creature gets +1/+0 until end of turn.", new CreatureColorTargetFilter(Set.of(CardColor.BLUE, CardColor.RED))));
    }
}
