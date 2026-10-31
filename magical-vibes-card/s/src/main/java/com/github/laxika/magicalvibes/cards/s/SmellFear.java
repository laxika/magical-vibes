package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH2", collectorNumber = "173")
public class SmellFear extends Card {

    public SmellFear() {
        addSpellEffect(new ProliferateEffect());

        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addSpellEffect(new FightTargetsEffect());
    }
}
