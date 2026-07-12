package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "153")
public class PhyrexianPlaguelord extends Card {

    public PhyrexianPlaguelord() {
        // {T}, Sacrifice this creature: Target creature gets -4/-4 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new BoostTargetCreatureEffect(-4, -4)),
                "{T}, Sacrifice Phyrexian Plaguelord: Target creature gets -4/-4 until end of turn."
        ));

        // Sacrifice a creature: Target creature gets -1/-1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeCreatureCost(), new BoostTargetCreatureEffect(-1, -1)),
                "Sacrifice a creature: Target creature gets -1/-1 until end of turn."
        ));
    }
}
