package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleSelfPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDM", collectorNumber = "180")
public class DragonclawStrike extends Card {

    public DragonclawStrike() {
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new DoubleSelfPowerToughnessEffect());
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addSpellEffect(new FightTargetsEffect());
    }
}
