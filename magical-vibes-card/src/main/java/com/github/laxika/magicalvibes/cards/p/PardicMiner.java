package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantPlayLandsThisTurnEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "212")
public class PardicMiner extends Card {

    public PardicMiner() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new TargetPlayerCantPlayLandsThisTurnEffect()),
                "Sacrifice this creature: Target player can't play lands this turn."
        ));
    }
}
