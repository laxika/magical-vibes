package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CreatureColorTargetFilter;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "46")
public class SpiritWeaver extends Card {

    public SpiritWeaver() {
        addActivatedAbility(new ActivatedAbility(false, "{2}", List.of(new BoostTargetCreatureEffect(0, 1)), true, "{2}: Target green or blue creature gets +0/+1 until end of turn.", new CreatureColorTargetFilter(Set.of(CardColor.GREEN, CardColor.BLUE))));
    }
}
