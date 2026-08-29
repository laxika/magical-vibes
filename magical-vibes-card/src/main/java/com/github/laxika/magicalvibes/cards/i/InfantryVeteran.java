package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M11", collectorNumber = "18")
@CardRegistration(set = "BRB", collectorNumber = "34")
@CardRegistration(set = "9ED", collectorNumber = "21")
@CardRegistration(set = "6ED", collectorNumber = "26")
@CardRegistration(set = "VIS", collectorNumber = "9")
@CardRegistration(set = "ATH", collectorNumber = "8")
public class InfantryVeteran extends Card {

    public InfantryVeteran() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new BoostTargetCreatureEffect(1, 1)),
                "{T}: Target attacking creature gets +1/+1 until end of turn.",
                TargetFilters.attackingCreature()));
    }
}
